package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.glutils.FileTextureData
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.LoadTileObjectListener
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.property
import ktx.tiled.width

class SpawnSystem(
    assets: AssetManager = inject(),
) : IntervalSystem(enabled = false), LoadTileObjectListener {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]

    override fun onTick() = Unit

    override fun onLoadTileObject(
        x: Float,
        y: Float,
        mapObject: TiledMapTileMapObject,
        tile: TiledMapTile
    ) {
        val w = mapObject.width.toWorldUnits()
        val h = mapObject.height.toWorldUnits()
        val z = mapObject.property("z", 0)
        val data = tile.textureRegion.texture.textureData as FileTextureData
        val atlasKey = data.fileHandle.pathWithoutExtension()
            .substringAfter("graphics/sprites/") // atlas key is without TexturePacker input directory name
            .substringBeforeLast("_") // remove index like "idle_0" becomes "idle"

        world.entity {
            it += Transform(position = vec2(x, y), size = vec2(w, h), z = z)
            it += Graphic(objectsAtlas.findRegions(atlasKey).first())
            it += EntityTag.ACTIVE
        }
    }
}