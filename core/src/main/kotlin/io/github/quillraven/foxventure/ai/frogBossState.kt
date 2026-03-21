package io.github.quillraven.foxventure.ai

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Animation.Companion.getGdxAnimation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Invulnerable
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Platform
import io.github.quillraven.foxventure.component.Transform
import ktx.app.gdxError
import ktx.collections.gdxArrayOf
import ktx.math.vec2

private val PHASE1_2_WAYPOINTS =
    listOf("left", "platform-left", "center-left", "center-right", "platform-right", "right")
private val PHASE3_WAYPOINTS = listOf("left", "center-left", "center", "center-right", "right")

data object FrogBossStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }
}

data object FrogBossStateJump : FsmState {
    private fun waypointsForPhase(phase: Int, rtl: Boolean): List<String> {
        val list = if (phase < 2) PHASE1_2_WAYPOINTS else PHASE3_WAYPOINTS
        // rtl = right-to-left: iterate reversed, skip last (= starting point "left")
        // ltr = left-to-right: iterate normal, skip first (= starting point "left")
        return if (rtl) list.dropLast(1).asReversed() else list.drop(1)
    }

    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.JUMP)

        val fsm = entity[Fsm]
        val waypoints = fsm.customProperty<Map<String, Vector2>>("waypoints")
        val jumpDurations = fsm.customProperty<FloatArray>("jump_duration")
        val direction = fsm.customProperty<Int>("direction")
        val platformsDestroyed = fsm.customProperty<Boolean>("platforms_destroyed")
        val phase = fsm.customProperty<Int>("phase")
        val rtl = direction == -1

        // destroy platforms on first entry into phase 3
        if (phase == 2 && !platformsDestroyed) {
            fsm.customProperties["platforms_destroyed"] = true
            family { all(Platform) }.forEach { it.remove() }
        }

        val jumpDuration = jumpDurations[phase]
        val halfDuration = jumpDuration * 0.5f
        val jumpHeight = 3f
        val collBox = entity[Collision].box
        val names = waypointsForPhase(phase, rtl)
        val landingYs = names.map { name ->
            val pt = waypoints[name] ?: gdxError("No waypoint for name $name in phase $phase")
            pt.y - collBox.y
        }
        val points = gdxArrayOf<MoveToPoint>()
        names.forEachIndexed { i, name ->
            val pt = waypoints[name] ?: return@forEachIndexed
            val landX = pt.x - collBox.x - collBox.width * 0.5f
            val landY = landingYs[i]
            val startY = if (i == 0) entity[Transform].position.y else landingYs[i - 1]
            val peakY = maxOf(startY, landY) + jumpHeight
            points.add(MoveToPoint(vec2(landX, peakY), Interpolation.smooth, halfDuration, Interpolation.pow3Out))
            points.add(MoveToPoint(vec2(landX, landY), Interpolation.smooth, halfDuration, Interpolation.pow3In))
        }

        entity.configure {
            it += MoveTo(points)
            it += Invulnerable(999f)
        }
    }

    override fun World.onUpdate(entity: Entity) {
        val moveTo = entity.getOrNull(MoveTo)
        if (moveTo == null) {
            entity[Fsm].state.changeState(FrogBossStateVulnerable)
            return
        }

        // rising = even pointIdx (peak target), falling = odd pointIdx (land target)
        val rising = moveTo.pointIdx % 2 == 0
        val anim = entity[Animation]
        val targetType = if (rising) AnimationType.JUMP else AnimationType.FALL
        if (anim.activeType != targetType) anim.changeTo(targetType)
    }
}

data object FrogBossStateVulnerable : FsmState {
    private fun World.spawnStunSfx(entity: Entity): Entity {
        val objectsAtlas = inject<TextureAtlas>()
        val stunAnimation = objectsAtlas.getGdxAnimation("sfx/stun", AnimationType.IDLE)
        val stunFrame = stunAnimation.keyFrames.first()
        val (bossPos, _) = entity[Transform]
        val collBox = entity[Collision].box
        val stunW = stunFrame.regionWidth.toWorldUnits() * 1.75f
        val stunH = stunFrame.regionHeight.toWorldUnits() * 1.75f
        val collTop = bossPos.y + collBox.y + collBox.height

        return entity {
            it += Transform(
                position = vec2(bossPos.x + collBox.x + collBox.width * 0.5f - stunW * 0.5f, collTop),
                size = vec2(stunW, stunH),
                z = Transform.Z_SFX,
            )
            it += Graphic(stunFrame)
            it += Animation("sfx/stun", stunAnimation, emptyMap(), 1f)
            it += DelayRemoval(stunAnimation.animationDuration * 10f)
            it += EntityTag.ACTIVE
        }
    }

    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
        entity.configure { it -= Invulnerable }

        val stunEntity = spawnStunSfx(entity)
        val fsm = entity[Fsm]
        fsm.customProperties["stun_entity"] = stunEntity

        // record life at entry so we can detect a hit exactly once
        fsm.customProperties["life_on_enter"] = entity[Life].amount
    }

    override fun World.onUpdate(entity: Entity) {
        val fsm = entity[Fsm]
        val life = entity[Life].amount

        if (life <= 0f) return

        val phase = fsm.customProperty<Int>("phase")
        if (life < fsm.customProperty<Float>("life_on_enter")) {
            fsm.customProperty<Entity>("stun_entity").let { stunEntity ->
                if (!stunEntity.wasRemoved()) stunEntity.remove()
                fsm.customProperties["stun_entity"] = Entity.NONE
            }
            val color = entity[Graphic].color
            color.g -= 0.2f
            color.b -= 0.2f
            fsm.customProperties["phase"] = phase + 1
            fsm.state.changeState(FrogBossStateRecovery)
            return
        }

        val vulnerableDuration = fsm.customProperty<FloatArray>("vulnerable_duration")[phase]
        if (fsm.state.stateTime >= vulnerableDuration) {
            val direction = fsm.customProperty<Int>("direction")
            fsm.customProperties["direction"] = direction * -1
            fsm.state.changeState(FrogBossStateJump)
        }
    }
}

data object FrogBossStateRecovery : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
        entity.configure { it += Invulnerable(999f) }
    }

    override fun World.onUpdate(entity: Entity) {
        val fsm = entity[Fsm]

        if (fsm.state.stateTime >= 3f) {
            val direction = fsm.customProperty<Int>("direction")
            fsm.customProperties["direction"] = direction * -1
            fsm.state.changeState(FrogBossStateJump)
        }
    }
}
