package io.github.quillraven.foxventure.ai

import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.ProximityDetector
import io.github.quillraven.foxventure.component.Transform
import ktx.collections.gdxArrayOf
import ktx.math.vec2

data object EagleStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val fsm = entity[Fsm]
        val target = entity[ProximityDetector].target
        if (fsm.state.stateTime < 2.5f || target.wasRemoved()) {
            return
        }

        fsm.state.changeState(EagleStateAttack)
    }
}

data object EagleStateAttack : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.ATTACK)

        val target = entity[ProximityDetector].target
        if (target.wasRemoved() || target hasNo Collision) {
            // e.g., player died already -> go back to idle
            entity[Fsm].state.changeState(EagleStateIdle)
            return
        }

        val targetTransform = target[Transform]
        val targetCollision = target[Collision]

        val eagleTransform = entity[Transform]
        val eagleX = eagleTransform.position.x
        val eagleY = eagleTransform.position.y

        val targetCenterX = targetTransform.position.x + targetCollision.box.x + targetCollision.box.width * 0.5f
        val targetY = targetTransform.position.y + targetCollision.box.y + targetCollision.box.height * 0.25f
        val distanceX = targetCenterX - eagleX
        val mirroredX = targetCenterX + distanceX

        entity.configure {
            it += MoveTo(
                points = gdxArrayOf(
                    MoveToPoint(vec2(targetCenterX, targetY), Interpolation.linear, 1.2f, Interpolation.pow3Out),
                    MoveToPoint(vec2(targetCenterX, targetY), Interpolation.linear, 0.3f),
                    MoveToPoint(vec2(mirroredX, eagleY), Interpolation.linear, 0.8f, Interpolation.pow3In)
                )
            )
        }
    }

    override fun World.onUpdate(entity: Entity) {
        if (entity.getOrNull(MoveTo) == null) {
            entity[Fsm].state.changeState(EagleStateIdle)
        }
    }
}
