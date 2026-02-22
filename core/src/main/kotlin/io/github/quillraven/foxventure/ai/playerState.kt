package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command

data object PlayerStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].let { it.changeTo(it.idle) }
    }

    override fun World.onUpdate(entity: Entity) {
        val controller = entity[Controller]
        val collision = entity[Collision]
        val velocity = entity[Velocity].current

        // Check ladder first - highest priority when on ladder
        if (collision.isOnLadder && controller.hasAnyCommand(Command.MOVE_DOWN, Command.MOVE_UP)) {
            entity[Fsm].state.changeState(PlayerStateClimb)
        } else if (velocity.x != 0f || controller.hasAnyCommand(Command.MOVE_LEFT, Command.MOVE_RIGHT)) {
            entity[Fsm].state.changeState(PlayerStateRun)
        } else if (velocity.y > 0f) {
            entity[Fsm].state.changeState(PlayerStateJump)
        } else if (velocity.y < 0f) {
            entity[Fsm].state.changeState(PlayerStateFall)
        }
    }
}

data object PlayerStateRun : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].let { it.changeTo(it.run) }
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity].current
        val controller = entity[Controller]

        if (velocity.x == 0f && controller.hasNoCommand(Command.MOVE_LEFT, Command.MOVE_RIGHT)) {
            entity[Fsm].state.changeState(PlayerStateIdle)
        } else if (velocity.y > 0f) {
            entity[Fsm].state.changeState(PlayerStateJump)
        } else if (velocity.y < 0f) {
            entity[Fsm].state.changeState(PlayerStateFall)
        }
    }
}

data object PlayerStateJump : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].let { it.changeTo(it.jump) }
    }

    override fun World.onUpdate(entity: Entity) {
        val collision = entity[Collision]
        val controller = entity[Controller]

        if (collision.isOnLadder && controller.hasAnyCommand(Command.MOVE_DOWN, Command.MOVE_UP)) {
            entity[Fsm].state.changeState(PlayerStateClimb)
        } else if (entity[Velocity].current.y <= 0f) {
            entity[Fsm].state.changeState(PlayerStateFall)
        }
    }
}

data object PlayerStateFall : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].let { it.changeTo(it.fall) }
    }

    override fun World.onUpdate(entity: Entity) {
        val collision = entity[Collision]
        val controller = entity[Controller]

        if (collision.isOnLadder && controller.hasAnyCommand(Command.MOVE_DOWN, Command.MOVE_UP)) {
            entity[Fsm].state.changeState(PlayerStateClimb)
        } else if (collision.isGrounded) {
            entity[Fsm].state.changeState(PlayerStateIdle)
        } else if (entity[Velocity].current.y > 0f) {
            entity[Fsm].state.changeState(PlayerStateJump)
        }
    }
}

data object PlayerStateClimb : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].let { it.changeTo(it.climb) }
    }

    override fun World.onExit(entity: Entity) {
        entity[Animation].speed = 1f
    }

    override fun World.onUpdate(entity: Entity) {
        val velocityY = entity[Velocity].current.y
        if (!entity[Collision].isOnLadder) {
            when {
                velocityY < 0f -> entity[Fsm].state.changeState(PlayerStateFall)
                velocityY > 0f -> entity[Fsm].state.changeState(PlayerStateJump)
                else -> entity[Fsm].state.changeState(PlayerStateIdle)
            }
        } else {
            entity[Animation].speed = if (velocityY == 0f) 0f else 1f
        }
    }
}
