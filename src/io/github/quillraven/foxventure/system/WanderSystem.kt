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

        if (wander.moveDirection == 0f) {
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
        if (abs(distance) >= wander.distance && sign(distance) != sign(wander.moveDirection)) {
            wander.moveDirection = -wander.moveDirection
            return
        }

        // ... and if no -> continue moving unless stopping at a cliff is desired
        if (wander.stopAtCliff && !tiledService.isGroundAhead(position, collisionBox, wander.moveDirection)) {
            wander.moveDirection = -wander.moveDirection
            return
        }
    }
}