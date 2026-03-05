package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.CameraShake
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.tiled.TiledService
import ktx.math.component1
import ktx.math.component2

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
        var targetX = position.x + size.x * 0.5f
        var targetY = position.y + size.y * 0.5f

        entity.getOrNull(CameraShake)?.let { shake ->
            val completion = shake.currentDuration / shake.duration
            val inverseCompletion = 1f - completion
            val power = shake.max * inverseCompletion * inverseCompletion
            shake.currentDuration += deltaTime
            if (shake.currentDuration >= shake.duration) {
                entity.configure { it -= CameraShake }
            } else {
                targetX += MathUtils.random(-1f, 1f) * power
                targetY += MathUtils.random(-1f, 1f) * power
            }
        }

        // Lerp camera position
        camera.position.x = MathUtils.lerp(camera.position.x, targetX, lerpSpeed * deltaTime)
        camera.position.y = MathUtils.lerp(camera.position.y, targetY, lerpSpeed * deltaTime)

        clampCamera()
        camera.update()
    }

    override fun onMapChanged(tiledMap: TiledMap) {
        // Instantly move the camera to focus entity when a map changes
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
        val halfW = gameViewport.worldWidth * 0.5f
        val halfH = gameViewport.worldHeight * 0.5f
        val (camX, camY) = camera.position

        camera.position.x = camX.coerceIn(halfW, maxOf(halfW, tiledService.mapWidth - halfW))
        camera.position.y = camY.coerceIn(halfH, maxOf(halfH, tiledService.mapHeight - halfH))
    }
}
