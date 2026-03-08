package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Defines the spatial properties of an entity including [position], [size], [rotationDegrees], [scale], and [z] depth.
 * Higher [z] values render on top of lower values. Entities are sorted by z-order, then by y-position, then by x-position.
 */
data class Transform(
    val position: Vector2,
    val size: Vector2,
    val rotationDegrees: Float = 0f,
    val scale: Float = 1f,
    var z: Int = 0,
) : Component<Transform>, Comparable<Transform> {
    override fun type() = Transform

    override fun compareTo(other: Transform): Int {
        val zCmp = this.z.compareTo(other.z)
        if (zCmp != 0) return zCmp
        val yCmp = other.position.y.compareTo(this.position.y)
        if (yCmp != 0) return yCmp
        return this.position.x.compareTo(other.position.x)
    }

    companion object : ComponentType<Transform>() {
        fun zByTiledType(tiledType: String) = when (tiledType) {
            "player" -> Z_PLAYER
            "enemy" -> Z_ENEMY
            else -> 0
        }

        const val Z_SFX = 30
        const val Z_PLAYER = 20
        const val Z_ENEMY = 10
    }
}