package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.MapChangeListener
import ktx.app.gdxError
import ktx.collections.gdxArrayOf
import ktx.math.component1
import ktx.math.component2
import ktx.tiled.height
import ktx.tiled.width

private class Chunk {
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
        return "Chunk@${hashCode()}(bounds=$bounds)"
    }
}

class ActivationSystem(
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(
    family = family { all(Transform).none(Player) },
    interval = Fixed(1 / 20f),
), MapChangeListener {
    private val chunks = gdxArrayOf<Chunk>()
    private val entityToChunk = mutableMapOf<Entity, Chunk>()
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

        // on the first onTickEntity call the entity is not in any chunk, and therefore entityToChunk returns null
        val currentChunk = entityToChunk[entity]
        if (currentChunk != newChunk) {
            currentChunk?.entities?.removeValue(entity, true)
            newChunk.entities.add(entity)
            entityToChunk[entity] = newChunk
        }
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
            chunk.entities.forEach { entity ->
                if (isVisible) {
                    entity.configure { it += EntityTag.ACTIVE }
                } else {
                    entity.configure { it -= EntityTag.ACTIVE }
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
    }

    companion object {
        private const val CHUNK_WIDTH = 21f
        private const val CHUNK_HEIGHT = 9f
        private const val BUFFER = 2f
    }
}
