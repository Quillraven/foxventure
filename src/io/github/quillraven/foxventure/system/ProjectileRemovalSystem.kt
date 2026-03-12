package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.TiledService

class ProjectileRemovalSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(EntityTag.PROJECTILE) }
) {
    override fun onTickEntity(entity: Entity) {
        val (position) = entity[Transform]

        if (position.x < 0f || position.x > tiledService.mapWidth) {
            // out of map boundaries -> remove it
            entity.remove()
        } else if (entity[Velocity].current.x == 0f) {
            // colliding with solid -> remove it
            entity.remove()
        }
    }
}