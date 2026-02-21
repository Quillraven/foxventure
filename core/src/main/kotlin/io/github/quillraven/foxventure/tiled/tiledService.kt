package io.github.quillraven.foxventure.tiled

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Rectangle
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.MapAsset
import ktx.app.gdxError
import ktx.assets.loadAsset
import ktx.collections.gdxArrayOf
import ktx.tiled.height
import ktx.tiled.layer
import ktx.tiled.property
import ktx.tiled.width
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
    private val currentTileLayers = gdxArrayOf<TiledMapTileLayer>()
    private val mapChangeListeners = gdxArrayOf<MapChangeListener>()
    private val loadTileObjectListeners = gdxArrayOf<LoadTileObjectListener>()

    val mapWidth: Int get() = currentMap.width

    val mapHeight: Int get() = currentMap.height

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
        currentMap.layers.getByType(TiledMapTileLayer::class.java, currentTileLayers)

        // load objects
        loadObjects()

        // notify listeners
        mapChangeListeners.forEach { it.onMapChanged(currentMap) }
    }

    private fun loadObjects() {
        currentMap.layer("objects").objects.forEach { mapObject ->
            val x = mapObject.x.toWorldUnits()
            val y = mapObject.y.toWorldUnits()
            if (mapObject is TiledMapTileMapObject) {
                loadTileObjectListeners.forEach { it.onLoadTileObject(x, y, mapObject, mapObject.tile) }
            } else {
                gdxError("Unsupported map object $mapObject")
            }
        }
    }

    fun getCollisionRect(cellX: Int, cellY: Int, includeSemiSolid: Boolean, includeLadder: Boolean, result: Rectangle) {
        result.set(0f, 0f, 0f, 0f)
        if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return

        currentTileLayers.forEach { layer ->
            val cell = layer.getCell(cellX, cellY) ?: return@forEach
            val tileType = cell.tile.property<String>("type", "")
            if (!includeSemiSolid && tileType == "semisolid") return@forEach
            if (!includeLadder && tileType == "ladder") return@forEach
            val mapObject = cell.tile.objects.singleOrNull() ?: return@forEach

            result.set(
                cellX + mapObject.x.toWorldUnits(),
                cellY + mapObject.y.toWorldUnits(),
                mapObject.width.toWorldUnits(),
                mapObject.height.toWorldUnits()
            )
            return
        }
    }

    fun getLadderRect(cellX: Int, cellY: Int, result: Rectangle) {
        result.set(0f, 0f, 0f, 0f)
        if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return

        currentTileLayers.forEach { layer ->
            val cell = layer.getCell(cellX, cellY) ?: return@forEach
            if (cell.tile.property<String>("type", "") != "ladder") return@forEach
            val mapObject = cell.tile.objects.singleOrNull() ?: return@forEach

            result.set(
                cellX + mapObject.x.toWorldUnits(),
                cellY + mapObject.y.toWorldUnits(),
                mapObject.width.toWorldUnits(),
                mapObject.height.toWorldUnits()
            )
            return
        }
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
