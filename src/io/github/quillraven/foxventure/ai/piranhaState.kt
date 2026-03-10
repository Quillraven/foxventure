package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Fsm
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
        if (entity[Animation].isFinished()) {
            entity[Attack].resetCooldown()
            entity[Fsm].state.changeState(PiranhaStateIdle)
        }
    }
}