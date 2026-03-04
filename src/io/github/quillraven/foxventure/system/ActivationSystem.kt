package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.MapChangeListener
import ktx.app.gdxError
import ktx.collections.gdxArrayOf
import ktx.collections.gdxMapOf
import ktx.math.component1
import ktx.math.component2
import ktx.tiled.height
import ktx.tiled.width

private class Chunk {
    var active = false
    val entities = gdxArrayOf<Entity>()
    val bounds = Rectangle()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chunk
        return bounds == other.bounds
    }

    override fun hashCode(): Int {
        return bounds.hashCode()
    }

    override fun toString(): String {
        return "Chunk@${hashCode()}(bounds=$bounds, active=$active)"
    }
}

/**
 * The `ActivationSystem` manages the activation and deactivation of entities in the game world
 * based on the current viewport visibility. It divides the game world into chunks and toggles
 * entities' active state depending on their presence within the camera's visible area.
 *
 * Entities in chunks outside the visible area are deactivated to optimize performance,
 * while entities inside the visible area are activated.
 *
 * Implements the `MapChangeListener` interface to update chunks when the game map changes.
 *
 * ### Functionality:
 * - Tracks entity chunks based on their positions and the chunk dimensions defined by `CHUNK_WIDTH` and `CHUNK_HEIGHT`.
 * - Updates entities between chunks when their position changes.
 * - Deactivates entities not within the visible camera bounds.
 * - Reactivates entities entering the visible camera bounds.
 * - Handles the removal of entities to clean up associated chunk data.
 *
 * ### Map Integration:
 * It integrates with the game map through the `onMapChanged` method, which recalculates
 * chunks based on the updated map size and reassigns entities to appropriate chunks.
 *
 * ### Internals:
 * - `updateActivation` determines active chunks and toggles entity states.
 * - `getChunkAt` calculates and retrieves the appropriate chunk for a given position.
 * - `BUFFER` provides an additional boundary around the camera for preloading entities.
 */
class ActivationSystem(private val gameViewport: Viewport) : IteratingSystem(
    family = family { all(Transform, EntityTag.ACTIVE).none(Player, DelayRemoval) },
    interval = Fixed(1 / 20f),
), MapChangeListener {
    private val chunks = gdxArrayOf<Chunk>()
    private val entityToChunk = gdxMapOf<Int, Chunk>()
    private val visibleRect = Rectangle()
    private var chunksX = 0

    override fun onTick() {
        // update entity chunks
        super.onTick()
        updateActivation()
    }

    override fun onTickEntity(entity: Entity) {
        val transform = entity[Transform]
        val (x, y) = transform.position
        val newChunk = getChunkAt(x, y)

        val currentChunk = entityToChunk[entity.id] ?: gdxError("Entity $entity is not part of any chunk")
        if (currentChunk != newChunk) {
            currentChunk.entities.removeValue(entity, true)
            newChunk.entities.add(entity)
            entityToChunk.put(entity.id, newChunk)
        }
    }

    fun onRemoveEntity(entity: Entity) {
        entityToChunk.remove(entity.id)?.entities?.removeValue(entity, true)
    }

    private fun updateActivation() {
        val cam = gameViewport.camera
        visibleRect.set(
            cam.position.x - cam.viewportWidth / 2 - BUFFER,
            cam.position.y - cam.viewportHeight / 2 - BUFFER,
            cam.viewportWidth + BUFFER * 2,
            cam.viewportHeight + BUFFER * 2
        )

        chunks.forEach { chunk ->
            if (chunk.entities.isEmpty) return@forEach

            val isVisible = visibleRect.overlaps(chunk.bounds)
            if (isVisible != chunk.active) {
                chunk.active = isVisible
                chunk.entities.forEach { entity ->
                    if (isVisible) {
                        entity.configure { it += EntityTag.ACTIVE }
                    } else {
                        entity.configure { it -= EntityTag.ACTIVE }
                    }
                }
            }
        }
    }

    private fun getChunkAt(x: Float, y: Float): Chunk {
        val chunkX = (x / CHUNK_WIDTH).toInt()
        val chunkY = (y / CHUNK_HEIGHT).toInt()
        if (chunkX < 0 || chunkY < 0 || chunkX >= chunksX) gdxError("Invalid chunk index")
        val index = chunkY * chunksX + chunkX
        if (index < 0 || index >= chunks.size) gdxError("Invalid chunk index")
        val chunk = chunks[index]

        if (chunk.bounds.width == 0f) {
            chunk.bounds.set(
                chunkX * CHUNK_WIDTH,
                chunkY * CHUNK_HEIGHT,
                CHUNK_WIDTH,
                CHUNK_HEIGHT
            )
        }
        return chunk
    }

    override fun onMapChanged(tiledMap: TiledMap) {
        chunks.clear()
        entityToChunk.clear()

        val mapWidth = tiledMap.width
        val mapHeight = tiledMap.height
        chunksX = ((mapWidth + CHUNK_WIDTH - 1) / CHUNK_WIDTH).toInt()
        val chunksY = ((mapHeight + CHUNK_HEIGHT - 1) / CHUNK_HEIGHT).toInt()

        repeat(chunksX * chunksY) { chunks.add(Chunk()) }

        // Initial chunk assignment for all entities
        world.family { all(Transform).none(Player) }.forEach { entity ->
            val transform = entity[Transform]
            val (x, y) = transform.position
            val chunk = getChunkAt(x, y)
            chunk.entities.add(entity)
            entityToChunk.put(entity.id, chunk)
        }
    }

    companion object {
        private const val CHUNK_WIDTH = 24f
        private const val CHUNK_HEIGHT = 13f
        private const val BUFFER = 2f
    }
}
