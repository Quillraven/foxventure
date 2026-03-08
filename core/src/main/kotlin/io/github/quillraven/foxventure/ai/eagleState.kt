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
        val fsm = entity[Fsm]

        val target = entity[ProximityDetector].target
        if (target.wasRemoved() || target hasNo Collision) {
            // e.g., player died already -> go back to idle
            fsm.state.changeState(EagleStateIdle)
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

        val diveTime = fsm.customProperties["dive_time"] as Float
        val peakTime = fsm.customProperties["dive_peak_time"] as Float
        val riseTime = fsm.customProperties["rise_time"] as Float
        entity.configure {
            it += MoveTo(
                points = gdxArrayOf(
                    MoveToPoint(vec2(targetCenterX, targetY), Interpolation.linear, diveTime, Interpolation.pow3Out),
                    MoveToPoint(vec2(targetCenterX, targetY), Interpolation.linear, peakTime),
                    MoveToPoint(vec2(mirroredX, eagleY), Interpolation.linear, riseTime, Interpolation.pow3In)
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
