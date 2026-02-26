package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

class FollowSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Transform, Follow, Collision, Velocity, EntityTag.ACTIVE) }
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
        } else if (follow.target !in world) {
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
        val distance = abs(targetCenterX - centerX)
        // Hysteresis: use breakDistance when following, proximity when not
        val isFollowing = velocity.x != 0f
        val threshold = if (isFollowing) follow.breakDistance else follow.proximity
        val closestRange = 1.5f
        if (distance !in closestRange..threshold) {
            follow.moveDirection = 0f
            return
        }

        // check line of sight - ensure no solid tiles block the path
        if (hasObstacle(centerX.toInt(), centerY.toInt(), targetCenterX.toInt(), targetCenterY.toInt())) {
            follow.moveDirection = 0f
            return
        }

        // target in range -> set the move direction
        val direction = sign(targetCenterX - centerX)
        if (!follow.stopAtCliff) {
            follow.moveDirection = direction
            return
        }

        cliffDetection(position, collisionBox, direction, follow)
    }

    private fun cliffDetection(
        position: Vector2,
        collisionBox: Rect,
        direction: Float,
        follow: Follow
    ) {
        // Cliff detection - check closer to edge
        val checkTolerance = 0.1f // 1/10th of a world unit
        val checkDistance = if (direction > 0) collisionBox.width else 0f
        val checkX = (position.x + collisionBox.x + checkDistance + (direction * checkTolerance)).toInt()
        val checkY = (position.y + collisionBox.y - checkTolerance).toInt()

        val hasGroundAhead = tiledService.getCollisionRect(checkX, checkY, includeSemiSolid = true) != null
        if (!hasGroundAhead) {
            follow.moveDirection = 0f
            return
        }

        follow.moveDirection = direction
    }

    // Bresenham's algorithm
    private fun hasObstacle(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val sx = if (x1 < x2) 1 else -1
        val sy = if (y1 < y2) 1 else -1
        var err = dx - dy
        var x = x1
        var y = y1

        while (x != x2 || y != y2) {
            if (tiledService.getCollisionRect(x, y, includeSemiSolid = false) != null) {
                return true
            }
            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x += sx
            }
            if (e2 < dx) {
                err += dx
                y += sy
            }
        }
        return false
    }
}
