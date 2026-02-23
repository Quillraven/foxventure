package io.github.quillraven.foxventure.tiled

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
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

data class GroundTile(val type: String, val rect: Rect) {
    val isSolid get() = type == ""
    val isLadder get() = type == "ladder" || type == "ladder_top"
    val isLadderTop get() = type == "ladder_top"
    val isSemiSolid get() = type == "semisolid"
}

class TiledService(
    private val assets: AssetManager,
) {
    private var currentMap: TiledMap = TiledMap()
    private val groundTiles = GdxArray<GroundTile>()
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
                if (tileType == "ladder") {
                    // the top of a ladder has a special meaning in ClimbSystem.
                    // For simplicity, we will use a special "ladder_top" type instead of just "ladder"
                    // for future ladder queries
                    val cellAboveY = y + 1
                    if (cellAboveY >= currentMap.height) {
                        // topmost tile of the map -> ladder_top
                        groundTiles.add(GroundTile("ladder_top", collisionRect))
                        continue
                    }

                    val type = groundLayer.getCell(x, cellAboveY)?.tile?.property("type", "")
                    if (type != "ladder") {
                        // the tile above is no ladder -> ladder_top
                        groundTiles.add(GroundTile("ladder_top", collisionRect))
                        continue
                    }
                }
                groundTiles.add(GroundTile(tileType, collisionRect))
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

    fun getCollisionRect(position: Vector2, collisionBox: Rect, includeSemiSolid: Boolean): Rect? {
        val startX = position.x + collisionBox.x
        val endX = startX + collisionBox.width
        val startY = position.y + collisionBox.y
        val endY = position.y + collisionBox.y + collisionBox.height

        for (y in startY.toInt()..endY.toInt()) {
            for (x in startX.toInt()..endX.toInt()) {
                val rect = getCollisionRect(x, y, includeSemiSolid) ?: continue
                if (rect.overlaps(startX, startY, collisionBox.width, collisionBox.height)) {
                    return rect
                }
            }
        }
        return null
    }

    fun getCollisionRect(cellX: Int, cellY: Int, includeSemiSolid: Boolean): Rect? {
        if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return null

        val groundTile = groundTiles.get((cellY * currentMap.width) + cellX) ?: return null
        if (!includeSemiSolid && groundTile.isSemiSolid) return null
        // ignore ladders because you can run inside ladder tiles. Ladders are handled via separate methods.
        if (groundTile.isLadder) return null

        return groundTile.rect
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
                    when {
                        groundTile.isSolid && solidRect.width == 0f -> solidRect.set(groundTile.rect)
                        groundTile.isSemiSolid && semiSolidRect.width == 0f -> semiSolidRect.set(groundTile.rect)
                        groundTile.isLadderTop && topLadderRect.width == 0f -> topLadderRect.set(groundTile.rect)
                    }

                    if (solidRect.width > 0f && semiSolidRect.width > 0f && topLadderRect.width > 0f) {
                        return true
                    }
                }
            }
        }
        return foundAnyRect
    }

    fun getLadderTile(position: Vector2, collisionBox: Rect, includeTileBelow: Boolean): GroundTile? {
        val startX = position.x + collisionBox.x
        val endX = startX + collisionBox.width
        var startY = position.y + collisionBox.y
        var endY = position.y + collisionBox.y + collisionBox.height
        if (includeTileBelow) {
            startY -= 1
            endY += 1
        }

        for (y in startY.toInt()..endY.toInt()) {
            for (x in startX.toInt()..endX.toInt()) {
                val ladder = getLadderTile(x, y) ?: continue
                if (ladder.rect.overlaps(startX, startY, collisionBox.width, collisionBox.height)) {
                    return ladder
                }
            }
        }
        return null
    }

    fun getLadderTile(cellX: Int, cellY: Int): GroundTile? {
        if (cellX !in 0..<currentMap.width || cellY !in 0..<currentMap.height) return null

        val groundTile = groundTiles.get((cellY * currentMap.width) + cellX) ?: return null
        if (!groundTile.isLadder) return null

        return groundTile
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
