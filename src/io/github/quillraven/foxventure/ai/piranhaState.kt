package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.ProjectileCfg
import io.github.quillraven.foxventure.component.ProximityDetector

data object PiranhaStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val fsm = entity[Fsm]
        val target = entity[ProximityDetector].target
        if (target.wasRemoved() || entity[Attack].time > 0f) {
            return
        }

        fsm.state.changeState(PiranhaStateAttack)
    }
}

data object PiranhaStateAttack : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.ATTACK)
    }

    override fun World.onUpdate(entity: Entity) {
        val target = entity[ProximityDetector].target
        if (target.wasRemoved()) {
            entity[Attack].resetCooldown()
            entity[Fsm].state.changeState(PiranhaStateIdle)
            return
        }

        val animation = entity[Animation]
        if (animation.isLastFrame()) {
            entity[Fsm].state.changeState(PiranhaStateSpawnProjectile)
            return
        }
    }
}

data object PiranhaStateSpawnProjectile : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].speed = 0.1f
        val projectileCfg = entity[ProjectileCfg]

        entity {
            it += projectileCfg.toRequest(source = entity, target = entity[ProximityDetector].target)
        }
    }

    override fun World.onUpdate(entity: Entity) {
        val animation = entity[Animation]

        if (animation.isFinished()) {
            animation.resetSpeed()
            entity[Attack].resetCooldown()
            entity[Fsm].state.changeState(PiranhaStateIdle)
        }
    }
}