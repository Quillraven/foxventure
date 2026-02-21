package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Transform
import ktx.graphics.use

class DebugRenderSystem(
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(Transform) }
) {
    private val shapeRenderer = ShapeRenderer()

    override fun onTick() {
        gameViewport.apply()
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, gameViewport.camera.combined) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val (position, size, _, scale) = entity[Transform]
        shapeRenderer.color = Color.RED
        shapeRenderer.rect(position.x, position.y, size.x * scale, size.y * scale)

        entity.getOrNull(Collision)?.let { collision ->
            val (collX, collY, collW, collH) = collision.box
            shapeRenderer.color = Color.GREEN
            shapeRenderer.rect(position.x + collX, position.y + collY, collW, collH)
        }
    }

    override fun onDispose() {
        shapeRenderer.dispose()
    }
}