package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.tiled.TiledService

class CameraSystem(
    private val gameViewport: Viewport = inject(),
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(EntityTag.CAMERA_FOCUS, Transform) }
), MapChangeListener {
    private val lerpSpeed = 5f

    override fun onTickEntity(entity: Entity) {
        val (position, size) = entity[Transform]
        val camera = gameViewport.camera

        // Target position (center of entity)
        val targetX = position.x + size.x * 0.5f
        val targetY = position.y + size.y * 0.5f

        // Lerp camera position
        camera.position.x = MathUtils.lerp(camera.position.x, targetX, lerpSpeed * deltaTime)
        camera.position.y = MathUtils.lerp(camera.position.y, targetY, lerpSpeed * deltaTime)

        clampCamera()
        camera.update()
    }

    override fun onMapChanged(tiledMap: TiledMap) {
        // Instantly move camera to focus entity when a map changes
        family.forEach { entity ->
            val (position, size) = entity[Transform]
            val camera = gameViewport.camera

            camera.position.x = position.x + size.x * 0.5f
            camera.position.y = position.y + size.y * 0.5f

            clampCamera()
            camera.update()
        }
    }

    private fun clampCamera() {
        val camera = gameViewport.camera
        val halfViewWidth = gameViewport.worldWidth * 0.5f
        val halfViewHeight = gameViewport.worldHeight * 0.5f

        camera.position.x = camera.position.x.coerceIn(halfViewWidth, tiledService.mapWidth - halfViewWidth)
        camera.position.y = camera.position.y.coerceIn(halfViewHeight, tiledService.mapHeight - halfViewHeight)
    }
}
