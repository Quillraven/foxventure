package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.AttackRange
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damage
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Velocity
import ktx.math.vec2
import kotlin.math.abs

private fun World.checkAttack(entity: Entity): Boolean {
    val attackRange = entity[AttackRange]
    val follow = entity[Follow]

    if (attackRange.cooldown <= 0f && follow.target != Entity.NONE) {
        val (position) = entity[Transform]
        val collBox = entity[Collision].box
        val centerX = position.x + collBox.x + (collBox.width * 0.5f)

        val (targetPosition) = follow.target[Transform]
        val targetCollBox = follow.target[Collision].box
        val targetCenterX = targetPosition.x + targetCollBox.x + (targetCollBox.width * 0.5f)

        if (abs(targetCenterX - centerX) <= attackRange.range) {
            return true
        }
    }

    return false
}

data object MushroomStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity]

        if (checkAttack(entity)) {
            entity[Fsm].state.changeState(MushroomStateAttack)
        } else if (velocity.current.x != 0f) {
            entity[Fsm].state.changeState(MushroomStateRun)
        }
    }
}

data object MushroomStateRun : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.RUN)
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity]

        if (checkAttack(entity)) {
            entity[Fsm].state.changeState(MushroomStateAttack)
        } else if (velocity.current.x == 0f) {
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }
}

data object MushroomStateAttack : FsmState {
    private var idleWidth = 0f
    private var idleHeight = 0f
    private var attackWidth = 0f
    private var attackHeight = 0f
    private var widthDiff = 0f

    private fun initAnimationDimensions(animation: Animation) {
        if (idleWidth != 0f) return

        idleWidth = animation.idle.getKeyFrame(0f).regionWidth.toWorldUnits()
        idleHeight = animation.idle.getKeyFrame(0f).regionHeight.toWorldUnits()
        val attackAnim = animation.get(AnimationType.ATTACK)
        attackWidth = attackAnim.getKeyFrame(0f).regionWidth.toWorldUnits()
        attackHeight = attackAnim.getKeyFrame(0f).regionHeight.toWorldUnits()
        widthDiff = attackWidth - idleWidth
    }

    override fun World.onEnter(entity: Entity) {
        val animation = entity[Animation]
        initAnimationDimensions(animation)
        animation.changeTo(AnimationType.ATTACK)

        // adjust Transform/Graphic because attack frame has different dimensions than the idle/run frames
        val transform = entity[Transform]
        transform.size.set(attackWidth, attackHeight)
        val graphic = entity[Graphic]
        graphic.offset.x = if (graphic.flip) -widthDiff else 0f

        // stop the entity from moving
        entity.configure { it += EntityTag.ROOT }

        // spawn damage entity
        val collBox = entity[Collision].box
        val damageX = transform.position.x + collBox.x + (collBox.width * 0.5f)
        val damageY = transform.position.y + collBox.y
        val damageOffsetX = if (graphic.flip) -widthDiff - collBox.x else 0f
        spawnDamageEntity(damageX, damageY, damageOffsetX)
    }

    override fun World.onUpdate(entity: Entity) {
        if (entity[Animation].isFinished()) {
            entity[AttackRange].cooldown = 2f
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }

    override fun World.onExit(entity: Entity) {
        val transform = entity[Transform]
        transform.size.set(idleWidth, idleHeight)
        entity[Graphic].offset.x = 0f
        entity.configure { it -= EntityTag.ROOT }
    }

    private fun World.spawnDamageEntity(x: Float, y: Float, offsetX: Float) {
        entity {
            val size = vec2(1.9f, 1.5f)
            it += Transform(position = vec2(x + offsetX, y), size = size)
            it += Collision(box = Rect(0f, 0f, size.x, size.y))
            it += Damage(amount = 1)
            it += DelayRemoval(timer = 2f)
            it += Type("damage")
            it += EntityTag.ACTIVE
        }
    }
}