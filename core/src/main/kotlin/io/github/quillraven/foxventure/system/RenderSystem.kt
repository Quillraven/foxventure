package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Transform
import ktx.graphics.use

class RenderSystem(
    private val batch: Batch = inject(),
    private val stage: Stage = inject(),
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(Transform, Graphic, EntityTag.ACTIVE) },
    comparator = compareEntityBy(Transform),
) {

    override fun onTick() {
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) {
            super.onTick()
        }

        stage.viewport.apply()
        stage.act(deltaTime)
        stage.draw()
    }

    override fun onTickEntity(entity: Entity) {
        val (region, regionSize) = entity[Graphic]
        val (position, size, rotationDegrees, scale) = entity[Transform]

        // fill texture inside transform size by keeping the aspect ratio
        val realSize = Scaling.fill.apply(regionSize.x, regionSize.y, size.x, size.y)
        batch.draw(
            region,
            position.x, position.y,
            realSize.x / 2f, realSize.y / 2f,
            realSize.x, realSize.y,
            scale, scale,
            rotationDegrees
        )
    }
}
