package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.ProximityDetector
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs

/**
 * Manages attack cooldowns and determines when entities are ready to attack based on target proximity.
 */
class AttackSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Attack, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val attack = entity[Attack]
        if (attack.readyToAttack) return

        if (attack.time > 0f) {
            attack.time -= deltaTime
            return
        }

        // attack timer is zero -> ready to attack
        val target = entity.getOrNull(Follow)?.target
            ?: entity.getOrNull(ProximityDetector)?.target
            ?: Entity.NONE
        if (target.wasRemoved()) {
            attack.readyToAttack = true
            return
        }

        // ready to attack only if target is within attack range
        val (position) = entity[Transform]
        val collBox = entity[Collision].box
        val centerX = position.x + collBox.x + (collBox.width * 0.5f)
        val centerY = position.y + collBox.y + (collBox.height * 0.5f)

        val (targetPosition) = target[Transform]
        val targetCollBox = target[Collision].box
        val targetCenterX = targetPosition.x + targetCollBox.x + (targetCollBox.width * 0.5f)
        val targetCenterY = targetPosition.y + targetCollBox.y + (targetCollBox.height * 0.5f)

        val inRange = abs(targetCenterX - centerX) <= attack.range
        val hasLineOfSight = !tiledService.checkLineOfSight(centerX, centerY, targetCenterX, targetCenterY)
        attack.readyToAttack = inRange && hasLineOfSight

        if (attack.readyToAttack) {
            entity.getOrNull(Graphic)?.flip = targetCenterX < centerX
        }
    }
}
