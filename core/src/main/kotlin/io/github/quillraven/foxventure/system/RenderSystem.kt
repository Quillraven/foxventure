package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.MapChangeListener
import ktx.collections.gdxArrayOf
import ktx.tiled.use

class RenderSystem(
    private val batch: Batch = inject(),
    private val stage: Stage = inject(),
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(Transform, Graphic, EntityTag.ACTIVE) },
    comparator = compareEntityBy(Transform),
), MapChangeListener {

    private val camera: OrthographicCamera = gameViewport.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(TiledMap(), 1.toWorldUnits(), batch)
    private val bgdLayers = gdxArrayOf<MapLayer>()
    private val fgdLayers = gdxArrayOf<MapLayer>()

    override fun onTick() {
        gameViewport.apply()
        mapRenderer.use(camera) {
            bgdLayers.forEach(it::renderMapLayer)
            super.onTick() // render entities
            fgdLayers.forEach(it::renderMapLayer)
        }

        stage.viewport.apply()
        stage.act(deltaTime)
        stage.draw()
    }

    override fun onTickEntity(entity: Entity) {
        val graphic = entity[Graphic]
        val (region, regionSize) = graphic
        val (position, size, rotationDegrees, scale) = entity[Transform]

        // fill texture inside transform size by keeping the aspect ratio
        val targetW = size.x * scale
        val targetH = size.y * scale
        val realSize = Scaling.fill.apply(regionSize.x, regionSize.y, targetW, targetH)

        // flip graphic if moving left
        entity.getOrNull(Velocity)?.let { velocity ->
            if (velocity.current.x != 0f) {
                graphic.flip = velocity.current.x < 0f
            }
        }

        batch.draw(
            region,
            position.x, position.y,
            realSize.x / 2f, realSize.y / 2f,
            realSize.x, realSize.y,
            if (graphic.flip) -1f else 1f, 1f,
            rotationDegrees
        )
    }

    override fun onMapChanged(tiledMap: TiledMap) {
        mapRenderer.map = tiledMap

        bgdLayers.clear()
        fgdLayers.clear()
        var currentLayers = bgdLayers
        tiledMap.layers.forEach { layer ->
            if ("objects" == layer.name) {
                currentLayers = fgdLayers
                return@forEach
            }

            if (layer::class == MapLayer::class) {
                // ignore object layers
                return@forEach
            }

            currentLayers.add(layer)
        }
    }
}
