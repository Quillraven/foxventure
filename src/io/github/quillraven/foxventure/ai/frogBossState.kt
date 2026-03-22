package io.github.quillraven.foxventure.ai

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Animation.Companion.getGdxAnimation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.CameraShake
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.Dissolve
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Invulnerable
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Platform
import io.github.quillraven.foxventure.component.Transform
import ktx.collections.GdxArray
import ktx.math.vec2

data object FrogBossStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }
}

data object FrogBossStateJump : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.JUMP)

        val fsm = entity[Fsm]
        val phase = fsm.customProperty<Int>("phase")
        val rtl = fsm.customProperty<Int>("direction") == -1
        val dirIdx = if (rtl) 0 else 1

        if (phase == 2 && !fsm.customProperty<Boolean>("platforms_destroyed")) {
            fsm.customProperties["platforms_destroyed"] = true
            family { all(Platform) }.forEach { platform ->
                val region = platform[Graphic].region
                platform.configure {
                    it += Dissolve(
                        duration = 2f,
                        uvOffsetU = region.u,
                        uvOffsetV = region.v,
                        atlasMaxU = region.u2,
                        atlasMaxV = region.v2,
                    )
                    it += DelayRemoval(2f)
                }
            }
            family { all(EntityTag.CAMERA_FOCUS) }.first().configure {
                it += CameraShake(max = 8f, duration = 2f)
            }
        }

        // clone the pre-computed points so MoveTo can consume them independently each run
        val template = fsm.customProperty<Array<Array<GdxArray<MoveToPoint>>>>("jump_sequences")[phase][dirIdx]
        val points = GdxArray(template)

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
