package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Stun
import io.github.quillraven.foxventure.component.Velocity

data object PlayerStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val collision = entity[Collision]
        val velocity = entity[Velocity].current

        if (entity has Stun) {
            entity[Fsm].state.changeState(PlayerStateStunned)
        } else if (collision.isOnLadder) {
            entity[Fsm].state.changeState(PlayerStateClimb)
        } else if (velocity.x != 0f) {
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
        entity[Animation].changeTo(AnimationType.RUN)
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity]

        if (entity has Stun) {
            entity[Fsm].state.changeState(PlayerStateStunned)
        } else if (velocity.current.x == 0f) {
            entity[Fsm].state.changeState(PlayerStateIdle)
        } else if (velocity.current.y > 0f) {
            entity[Fsm].state.changeState(PlayerStateJump)
        } else if (velocity.current.y < 0f) {
            entity[Fsm].state.changeState(PlayerStateFall)
        } else if (velocity.isSkidding) {
            entity[Animation].let { animation ->
                animation.stateTime = animation.active.frameDuration * 3
            }
        }
    }
}

data object PlayerStateJump : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.JUMP)
    }

    override fun World.onUpdate(entity: Entity) {
        val collision = entity[Collision]

        if (entity has Stun) {
            entity[Fsm].state.changeState(PlayerStateStunned)
        } else if (collision.isOnLadder) {
            entity[Fsm].state.changeState(PlayerStateClimb)
        } else if (entity[Velocity].current.y <= 0f) {
            entity[Fsm].state.changeState(PlayerStateFall)
        }
    }
}

data object PlayerStateFall : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.FALL)
    }

    override fun World.onUpdate(entity: Entity) {
        val collision = entity[Collision]

        if (entity has Stun) {
            entity[Fsm].state.changeState(PlayerStateStunned)
        } else if (collision.isOnLadder) {
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
        entity[Animation].changeTo(AnimationType.CLIMB)
    }

    override fun World.onExit(entity: Entity) {
        entity[Animation].speed = 1f
    }

    override fun World.onUpdate(entity: Entity) {
        val velocityY = entity[Velocity].current.y

        if (entity has Stun) {
            entity[Fsm].state.changeState(PlayerStateStunned)
        } else if (!entity[Collision].isOnLadder) {
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

data object PlayerStateStunned : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.HURT)
    }

    override fun World.onUpdate(entity: Entity) {
        if (entity hasNo Stun) {
            entity[Fsm].state.changeState(PlayerStateIdle)
        }
    }
}

data object PlayerStateDeath : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.HURT)
    }
}
