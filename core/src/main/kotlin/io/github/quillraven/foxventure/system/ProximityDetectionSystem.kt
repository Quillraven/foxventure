package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.ProximityDetector
import io.github.quillraven.foxventure.component.Transform
import kotlin.math.abs

class ProximityDetectionSystem : IteratingSystem(
    family = family { all(Transform, Collision, ProximityDetector, EntityTag.ACTIVE) },
    interval = Fixed(1 / 20f),
) {
    private val candidatesFamily = family { all(Transform, Collision, EntityTag.ACTIVE) }

    override fun onTickEntity(entity: Entity) {
        val detector = entity[ProximityDetector]
        val (range, predicate, onDetect) = detector
        val (position) = entity[Transform]
        val (collBox) = entity[Collision]
        val centerX = position.x + collBox.x + (collBox.width * 0.5f)

        if (targetStillValid(centerX, detector.target, range)) {
            return
        }

        // find the new closest valid target
        detector.target = Entity.NONE
        var closestDistance = Float.MAX_VALUE
        candidatesFamily.forEach { candidate ->
            if (entity == candidate || !world.predicate(candidate)) return@forEach

            val (candidatePosition) = candidate[Transform]
            val (candidateCollBox) = candidate[Collision]
            val candidateCenterX = candidatePosition.x + candidateCollBox.x + (candidateCollBox.width * 0.5f)

            val distance = abs(centerX - candidateCenterX)
            if (distance <= range && distance < closestDistance) {
                detector.target = candidate
                closestDistance = distance
            }
        }

        if (detector.target != Entity.NONE) {
            onDetect(world, entity, detector.target)
        }
    }

    private fun targetStillValid(centerX: Float, target: Entity, range: Float): Boolean {
        if (target == Entity.NONE || target !in world) {
            return false
        }

        val (targetPosition) = target[Transform]
        val (targetCollBox) = target[Collision]
        val targetCenterX = targetPosition.x + targetCollBox.x + (targetCollBox.width * 0.5f)
        val distance = abs(centerX - targetCenterX)
        return distance <= range
    }
}
