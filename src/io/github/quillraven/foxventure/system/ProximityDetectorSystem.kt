package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.ProximityDetector
import io.github.quillraven.foxventure.component.Transform

class ProximityDetectorSystem : IteratingSystem(
    family = family { all(Transform, Collision, ProximityDetector, EntityTag.ACTIVE) },
    interval = Fixed(1 / 20f),
) {
    private val candidatesFamily = family { all(Transform, Collision, EntityTag.ACTIVE).none(DelayRemoval) }

    override fun onTickEntity(entity: Entity) {
        val detector = entity[ProximityDetector]
        val (squaredRange, predicate, onDetect, onBreak, target) = detector
        val (position) = entity[Transform]
        val (collBox) = entity[Collision]
        val centerX = position.x + collBox.x + (collBox.width * 0.5f)
        val centerY = position.y + collBox.y + (collBox.height * 0.5f)

        if (target != Entity.NONE && (target.wasRemoved() || calcDistance(centerX, centerY, target) > squaredRange)) {
            // target is no longer valid or out of range -> break
            onBreak(world, entity, target)
        } else if (target != Entity.NONE) {
            // target is still valid -> do nothing
            return
        }

        // find the new closest valid target
        detector.target = Entity.NONE
        var closestDistanceSq = Float.MAX_VALUE
        candidatesFamily.forEach { candidate ->
            if (entity == candidate || !world.predicate(candidate)) return@forEach

            val distanceSq = calcDistance(centerX, centerY, candidate)
            if (distanceSq <= squaredRange && distanceSq < closestDistanceSq) {
                detector.target = candidate
                closestDistanceSq = distanceSq
            }
        }

        if (detector.target != Entity.NONE) {
            onDetect(world, entity, detector.target)
        }
    }

    private fun calcDistance(centerX: Float, centerY: Float, target: Entity): Float {
        val (targetPosition) = target[Transform]
        val (targetCollBox) = target[Collision]
        val targetCenterX = targetPosition.x + targetCollBox.x + (targetCollBox.width * 0.5f)
        val targetCenterY = targetPosition.y + targetCollBox.y + (targetCollBox.height * 0.5f)
        val dx = centerX - targetCenterX
        val dy = centerY - targetCenterY
        return dx * dx + dy * dy
    }
}
