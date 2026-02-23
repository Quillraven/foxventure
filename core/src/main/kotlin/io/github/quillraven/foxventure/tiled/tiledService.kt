package io.github.quillraven.foxventure.tiled

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Rectangle
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.MapAsset
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Rect.Companion.set
import ktx.app.gdxError
import ktx.assets.loadAsset
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.tiled.height
import ktx.tiled.isEmpty
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

private data class GroundRect(val type: String, val rect: Rect) {
    val isLadder get() = type == "ladder"
    val isSemiSolid get() = type == "semisolid"
}

class TiledService(
    private val assets: AssetManager,
) {
    private var currentMap: TiledMap = TiledMap()
    private val groundTiles = GdxArray<GroundRect>()
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

        // load ground collision information
        loadGroundCollisionInfo()

        // load objects
        loadObjects()

        // notify listeners
        mapChangeListeners.forEach { it.onMapChanged(currentMap) }
    }

    private fun loadGroundCollisionInfo() {
        groundTiles.clear()

        val groundLayer = currentMap.layer("ground") as TiledMapTileLayer
        for (y in 0 until currentMap.height) {
            for (x in 0 until currentMap.width) {
                val cell = groundLayer.getCell(x, y)
                if (cell == null) {
                    // no cell -> no collision
                    groundTiles.add(null)
                    continue
                }

                val tile = cell.tile
                if (tile.objects.isEmpty()) {
                    // no collision objects defined for the cell -> no collision
                    groundTiles.add(null)
                    continue
                }

                // convert a single collision object to world units and store it according to 'type'
                val mapObject = tile.objects.single()
                val collisionRect = Rect(
                    x + mapObject.x.toWorldUnits(),
                    y + mapObject.y.toWorldUnits(),
                    mapObject.width.toWorldUnits(),
                    mapObject.height.toWorldUnits()
                )
                val tileType = tile.property("type", "")
                groundTiles.add(GroundRect(tileType, collisionRect))
            }
        }
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

        val groundTile = groundTiles.get((cellY * currentMap.width) + cellX) ?: return
        if (!includeSemiSolid && groundTile.isSemiSolid) return
        if (groundTile.isLadder) return

        result.set(groundTile.rect)
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

                val index = (y * currentMap.width) + x
                val groundTile = groundTiles.get(index) ?: continue

                if (groundTile.rect.overlaps(checkRect)) {
                    foundAnyRect = true
                    when (groundTile.type) {
                        "" if solidRect.width == 0f -> solidRect.set(groundTile.rect)
                        "semisolid" if semiSolidRect.width == 0f -> semiSolidRect.set(groundTile.rect)
                        "ladder" if topLadderRect.width == 0f && isTopLadderTile(x, y, true) -> {
                            topLadderRect.set(groundTile.rect)
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

        val groundTile = groundTiles.get((cellY * currentMap.width) + cellX) ?: return
        if (!groundTile.isLadder) return

        result.set(groundTile.rect)
    }

    fun isTopLadderTile(cellX: Int, cellY: Int, ignoreSelfCheck: Boolean = false): Boolean {
        if (!ignoreSelfCheck) {
            if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return false

            // Check if the current tile is a ladder
            val groundTile = groundTiles.get((cellY * currentMap.width) + cellX) ?: return false
            if (!groundTile.isLadder) {
                return false
            }
        }

        // Check if the tile above is NOT a ladder
        val cellAboveY = cellY + 1
        if (cellAboveY >= currentMap.height) return true

        val groundTile = groundTiles.get((cellAboveY * currentMap.width) + cellX) ?: return true
        if (groundTile.isLadder) {
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
