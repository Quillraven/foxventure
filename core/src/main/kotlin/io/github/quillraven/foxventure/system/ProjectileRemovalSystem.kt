package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.TiledService

class ProjectileRemovalSystem(
    private val tiledService: TiledService = inject(),
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(EntityTag.PROJECTILE) },
    interval = Fixed(1 / 20f),
) {
    override fun onTickEntity(entity: Entity) {
        val (position, size) = entity[Transform]

        when {
            // out of map boundaries -> remove it
            position.x + size.x < 0f || position.x > tiledService.mapWidth -> entity.remove()
            // colliding with solid -> remove it
            entity[Velocity].current.x == 0f -> entity.remove()
            // out of visible camera bounds -> remove it
            isOutsideCamera(position, size) -> entity.remove()
        }
    }

    private fun isOutsideCamera(position: Vector2, size: Vector2): Boolean {
        val camera = gameViewport.camera
        val halfW = gameViewport.worldWidth * 0.5f
        val halfH = gameViewport.worldHeight * 0.5f
        val camX = camera.position.x
        val camY = camera.position.y

        return position.x + size.x < camX - halfW
                || position.x > camX + halfW
                || position.y + size.y < camY - halfH
                || position.y > camY + halfH
    }
}