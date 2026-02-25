package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.AttackRange
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damage
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Tiled
import io.github.quillraven.foxventure.component.Transform
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
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.ATTACK)
        entity[Velocity].current.x = 0f
        entity[Follow].moveDirection = 0f

        val (position) = entity[Transform]
        val collBox = entity[Collision].box
        val centerX = position.x + collBox.x + (collBox.width * 0.5f)
        val centerY = position.y + collBox.y + (collBox.height * 0.5f)

        spawnDamageEntity(centerX, centerY)
    }

    override fun World.onUpdate(entity: Entity) {
        entity[Velocity].current.x = 0f
        entity[Follow].moveDirection = 0f

        if (entity[Animation].isFinished()) {
            entity[AttackRange].cooldown = 2f
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }

    private fun World.spawnDamageEntity(x: Float, y: Float) {
        entity {
            it += Transform(position = vec2(x - 0.5f, y - 0.5f), size = vec2(1f, 1f))
            it += Collision(box = Rect(0f, 0f, 1f, 1f))
            it += Damage(amount = 1)
            it += DelayRemoval(timer = 0.3f)
            it += Tiled(id = -1, type = "damage")
            it += EntityTag.ACTIVE
        }
    }
}