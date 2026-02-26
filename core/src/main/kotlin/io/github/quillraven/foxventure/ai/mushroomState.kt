package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.DamageRequest
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import ktx.math.vec2

data object MushroomStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity]

        if (entity[Attack].readyToAttack) {
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

        if (entity[Attack].readyToAttack) {
            entity[Fsm].state.changeState(MushroomStateAttack)
        } else if (velocity.current.x == 0f) {
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }
}

data object MushroomStateAttack : FsmState {
    override fun World.onEnter(entity: Entity) {
        val animation = entity[Animation]
        animation.changeTo(AnimationType.ATTACK)

        // stop the entity from moving
        entity.configure { it += EntityTag.ROOT }

        // move mushroom in front of player to render gas attack in front of player
        val transform = entity[Transform]
        transform.z = Transform.Z_PLAYER + 1

        // spawn damage entity
        val collBox = entity[Collision].box
        val damageX = transform.position.x + collBox.x + (collBox.width * 0.5f)
        val damageY = transform.position.y + collBox.y
        entity {
            it += DamageRequest(
                source = entity,
                damage = entity[Attack].damage,
                position = vec2(damageX, damageY),
                size = vec2(1.9f, 1.5f),
                lifeSpan = 2f
            )
        }
    }

    override fun World.onUpdate(entity: Entity) {
        if (entity[Animation].isFinished()) {
            entity[Attack].resetCooldown()
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }

    override fun World.onExit(entity: Entity) {
        entity.configure { it -= EntityTag.ROOT }

        // reset mushroom z position
        val transform = entity[Transform]
        transform.z = Transform.Z_ENEMY
    }
}