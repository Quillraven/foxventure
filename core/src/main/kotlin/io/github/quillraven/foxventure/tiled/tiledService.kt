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
    private var groundLayer = TiledMapTileLayer(0, 0, 0, 0)
    private val mapChangeListeners = gdxArrayOf<MapChangeListener>()
    private val loadTileObjectListeners = gdxArrayOf<LoadTileObjectListener>()
    private val tmpRect = Rectangle()

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
        groundLayer = currentMap.layer("ground") as TiledMapTileLayer

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

    fun getCollisionRect(cellX: Int, cellY: Int, includeSemiSolid: Boolean, result: Rectangle) {
        result.set(0f, 0f, 0f, 0f)
        if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return

        val cell = groundLayer.getCell(cellX, cellY) ?: return
        val tileType = cell.tile.property<String>("type", "")
        if (!includeSemiSolid && tileType == "semisolid") return
        if (tileType == "ladder") return
        val mapObject = cell.tile.objects.singleOrNull() ?: return

        result.set(
            cellX + mapObject.x.toWorldUnits(),
            cellY + mapObject.y.toWorldUnits(),
            mapObject.width.toWorldUnits(),
            mapObject.height.toWorldUnits()
        )
    }

    fun getAllCollisionRects(
        checkRect: Rectangle,
        solidRect: Rectangle,
        semiSolidRect: Rectangle,
        topLadderRect: Rectangle
    ): Boolean {
        solidRect.set(0f, 0f, 0f, 0f)
        semiSolidRect.set(0f, 0f, 0f, 0f)
        topLadderRect.set(0f, 0f, 0f, 0f)

        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()
        var foundAnyRect = false

        for (y in endY downTo startY) {
            for (x in startX..endX) {
                if (x !in 0..<currentMap.width || y !in 0..<currentMap.height) {
                    continue
                }

                val cell = groundLayer.getCell(x, y) ?: continue
                val tileType = cell.tile.property<String>("type", "")
                val mapObject = cell.tile.objects.singleOrNull() ?: continue
                tmpRect.set(
                    x + mapObject.x.toWorldUnits(),
                    y + mapObject.y.toWorldUnits(),
                    mapObject.width.toWorldUnits(),
                    mapObject.height.toWorldUnits()
                )

                if (tmpRect.overlaps(checkRect)) {
                    foundAnyRect = true
                    when (tileType) {
                        "" if solidRect.width == 0f -> solidRect.set(tmpRect)
                        "semisolid" if semiSolidRect.width == 0f -> semiSolidRect.set(tmpRect)
                        "ladder" if topLadderRect.width == 0f && isTopLadderTile(x, y, true) -> {
                            topLadderRect.set(tmpRect)
                        }
                    }

                    if (solidRect.width > 0f && semiSolidRect.width > 0f && topLadderRect.width > 0f) {
                        return true
                    }
                }
            }
        }
        return foundAnyRect
    }

    fun getLadderRect(cellX: Int, cellY: Int, result: Rectangle) {
        result.set(0f, 0f, 0f, 0f)
        if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return

        val cell = groundLayer.getCell(cellX, cellY) ?: return
        if (cell.tile.property<String>("type", "") != "ladder") return
        val mapObject = cell.tile.objects.singleOrNull() ?: return

        result.set(
            cellX + mapObject.x.toWorldUnits(),
            cellY + mapObject.y.toWorldUnits(),
            mapObject.width.toWorldUnits(),
            mapObject.height.toWorldUnits()
        )
    }

    fun isTopLadderTile(cellX: Int, cellY: Int, ignoreSelfCheck: Boolean = false): Boolean {
        if (!ignoreSelfCheck) {
            if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return false

            // Check if the current tile is a ladder
            val cell = groundLayer.getCell(cellX, cellY) ?: return false
            if (cell.tile.property("type", "") != "ladder") {
                return false
            }
        }

        // Check if the tile above is NOT a ladder
        val cellAboveY = cellY + 1
        if (cellAboveY >= currentMap.height) return true

        val cellAbove = groundLayer.getCell(cellX, cellAboveY) ?: return true
        if (cellAbove.tile.property("type", "") == "ladder") {
            return false // There's a ladder above, so not top
        }
        return true // No ladder above, this is the top
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
