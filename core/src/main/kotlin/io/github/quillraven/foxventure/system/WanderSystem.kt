package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Wander
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

/**
 * Makes entities wander within a distance from their origin, stopping at cliffs if configured.
 */
class WanderSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Wander, Physics, Collision, EntityTag.ACTIVE).none(Damaged, EntityTag.ROOT) }
) {
    override fun onTickEntity(entity: Entity) {
        val wander = entity[Wander]
        val follow = entity.getOrNull(Follow)

        if (follow != null && follow.target != Entity.NONE) {
            // entity is following another entity -> do not wander
            wander.moveDirection = 0f
            return
        }

        val wanderDirection = wander.moveDirection
        if (wanderDirection == 0f) {
            // no move direction set yet -> pick a random direction
            wander.moveDirection = if (MathUtils.randomBoolean()) 1f else -1f
            return
        }

        // wandering in a specific direction -> check if max wander range is reached ...
        val position = entity[Physics].position
        val (collisionBox) = entity[Collision]
        val centerX = position.x + collisionBox.x + collisionBox.width / 2f
        val distance = wander.originX - centerX
        // ... and if yes -> turn around
        if (abs(distance) >= wander.distance && sign(distance) != sign(wanderDirection)) {
            wander.moveDirection = -wanderDirection
            return
        }

        // ... and if no -> continue moving unless stopping at a wall ...
        val wallCheckTolerance = 0.1f
        val wallCheckDistance = when {
            wander.moveDirection > 0 -> (collisionBox.width + wallCheckTolerance)
            else -> -wallCheckTolerance
        }
        val checkX = (position.x + collisionBox.x + wallCheckDistance).toInt()
        val checkY = (position.y + collisionBox.y + collisionBox.height / 2f).toInt()
        val hasWall = tiledService.getCollisionRect(checkX, checkY, includeSemiSolid = false) != null
        if (hasWall) {
            wander.moveDirection = -wanderDirection
            return
        }
        // ... or a cliff if desired
        if (wander.stopAtCliff && !tiledService.isGroundAhead(position, collisionBox, wanderDirection)) {
            wander.moveDirection = -wanderDirection
            return
        }
    }
}