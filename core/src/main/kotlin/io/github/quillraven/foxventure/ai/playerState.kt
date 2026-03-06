package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Velocity

data object PlayerStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val collision = entity[Collision]
        val velocity = entity[Velocity].current
        val damaged = entity.getOrNull(Damaged)

        if (damaged != null && damaged.stunDuration > 0f) {
            entity[Fsm].state.changeState(PlayerStateHurt)
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
        val damaged = entity.getOrNull(Damaged)

        if (damaged != null && damaged.stunDuration > 0f) {
            entity[Fsm].state.changeState(PlayerStateHurt)
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
        val damaged = entity.getOrNull(Damaged)

        if (damaged != null && damaged.stunDuration > 0f) {
            entity[Fsm].state.changeState(PlayerStateHurt)
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
        val damaged = entity.getOrNull(Damaged)

        if (damaged != null && damaged.stunDuration > 0f) {
            entity[Fsm].state.changeState(PlayerStateHurt)
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
        val damaged = entity.getOrNull(Damaged)

        if (damaged != null && damaged.stunDuration > 0f) {
            entity[Fsm].state.changeState(PlayerStateHurt)
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

data object PlayerStateHurt : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.HURT)
    }

    override fun World.onUpdate(entity: Entity) {
        val damaged = entity.getOrNull(Damaged)
        if (damaged == null || damaged.stunDuration <= 0f) {
            entity[Fsm].state.changeState(PlayerStateIdle)
        }
    }
}

data object PlayerStateDeath : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.HURT)
    }
}
