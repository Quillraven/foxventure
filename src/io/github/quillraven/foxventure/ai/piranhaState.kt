package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.ProjectileCfg
import io.github.quillraven.foxventure.component.ProximityDetector
import io.github.quillraven.foxventure.component.Transform

data object PiranhaStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val fsm = entity[Fsm]
        val target = entity[ProximityDetector].target
        if (target.wasRemoved() || !entity[Attack].readyToAttack) {
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
        val target = entity[ProximityDetector].target

        val sourceCenterX = entity[Transform].position.x + entity[Collision].box.let { it.x + it.width * 0.5f }
        val targetCenterX = target[Transform].position.x + target[Collision].box.let { it.x + it.width * 0.5f }
        entity[Graphic].flip = targetCenterX < sourceCenterX

        entity {
            it += entity[ProjectileCfg].toRequest(source = entity, target = target)
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