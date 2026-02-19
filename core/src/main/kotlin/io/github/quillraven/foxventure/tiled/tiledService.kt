package io.github.quillraven.foxventure.tiled

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.MapAsset
import ktx.app.gdxError
import ktx.assets.loadAsset
import ktx.collections.gdxArrayOf
import ktx.tiled.layer
import ktx.tiled.property
import ktx.tiled.x
import ktx.tiled.y

interface MapChangeListener {
    fun onMapChanged(tiledMap: TiledMap)
}

interface LoadTileObjectListener {
    fun onLoadTileObject(x: Float, y: Float, mapObject: TiledMapTileMapObject, tile: TiledMapTile)
}

class TiledService(
    private val assets: AssetManager,
) {
    private var currentMap: TiledMap = TiledMap()
    private val mapChangeListeners = gdxArrayOf<MapChangeListener>()
    private val loadTileObjectListeners = gdxArrayOf<LoadTileObjectListener>()

    fun setMap(asset: MapAsset) {
        // unload the previous map from memory
        val mapPath = currentMap.property("gdxFilePath", "")
        if (mapPath.isNotBlank()) {
            assets.unload(mapPath)
        }

        // load the new map and set a special 'gdxFilePath' property for unloading
        val tiledMapAsset = assets.loadAsset(asset.descriptor)
        tiledMapAsset.finishLoading()
        currentMap = tiledMapAsset.asset
        currentMap.properties.put("gdxFilePath", asset.descriptor.fileName)

        // load objects
        currentMap.layer("objects").objects.forEach { mapObject ->
            val x = mapObject.x.toWorldUnits()
            val y = mapObject.y.toWorldUnits()
            if (mapObject is TiledMapTileMapObject) {
                loadTileObjectListeners.forEach { it.onLoadTileObject(x, y, mapObject, mapObject.tile) }
            } else {
                gdxError("Unsupported map object $mapObject")
            }
        }

        // notify listeners
        mapChangeListeners.forEach { it.onMapChanged(currentMap) }
    }

    fun addMapChangeListener(listener: MapChangeListener) {
        if (listener in mapChangeListeners) gdxError("MapChangeListener $listener is already registered")

        mapChangeListeners.add(listener)
    }

    fun addLoadTileObjectListener(listener: LoadTileObjectListener) {
        if (listener in loadTileObjectListeners) gdxError("LoadTileObjectListener $listener is already registered")

        loadTileObjectListeners.add(listener)
    }
}
