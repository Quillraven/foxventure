package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.sign

class FollowSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Transform, Follow, Collision, Velocity, EntityTag.ACTIVE).none(Damaged, EntityTag.ROOT) }
) {

    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val follow = entity[Follow]
        if (follow.target == Entity.NONE) {
            follow.moveDirection = 0f
            return
        } else if (follow.target.wasRemoved()) {
            follow.moveDirection = 0f
            follow.target = Entity.NONE
            return
        }

        val collisionBox = entity[Collision].box
        val velocity = entity[Velocity].current
        val (position) = entity[Transform]

        val (targetPosition) = follow.target[Transform]
        val targetCollisionBox = follow.target[Collision].box

        // check if the target is still in range
        val centerX = position.x + collisionBox.x + (collisionBox.width * 0.5f)
        val centerY = position.y + collisionBox.y + (collisionBox.height * 0.5f)
        val targetCenterX = targetPosition.x + targetCollisionBox.x + (targetCollisionBox.width * 0.5f)
        val targetCenterY = targetPosition.y + targetCollisionBox.y + (targetCollisionBox.height * 0.5f)
        val dx = centerX - targetCenterX
        val dy = centerY - targetCenterY
        val distance = dx * dx + dy * dy
        // Hysteresis: use breakDistance when following, proximity when not
        val isFollowing = velocity.x != 0f
        val threshold = if (isFollowing) follow.squaredBreakDistance else follow.squaredDistance
        val closestRange = 2.25f
        if (distance !in closestRange..threshold) {
            follow.moveDirection = 0f
            return
        }

        // check line of sight - ensure no solid tiles block the path
        if (tiledService.checkLineOfSight(centerX, centerY, targetCenterX, targetCenterY)) {
            follow.moveDirection = 0f
            return
        }

        // target in range -> set the move direction
        val direction = sign(targetCenterX - centerX)
        if (!follow.stopAtCliff || tiledService.isGroundAhead(position, collisionBox, direction)) {
            follow.moveDirection = direction
            return
        }

        follow.moveDirection = 0f
    }
}
