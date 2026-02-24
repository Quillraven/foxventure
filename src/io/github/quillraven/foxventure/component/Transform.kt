package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Represents a 2D transformation component that defines the spatial properties of an entity
 * in the game world. This includes position, size, rotation, scale, and depth order.
 *
 * @property position The position of the entity in 2D space, represented as a [Vector2].
 * @property size The dimensions of the entity, represented as a [Vector2].
 * @property rotationDegrees The rotational angle of the entity in degrees. Defaults to 0.
 * @property scale The scaling factor applied to the entity. Defaults to 1.
 * @property z The depth or layering order of the entity. A higher value is rendered on top of entities with lower values.
 */
data class Transform(
    val position: Vector2,
    val size: Vector2,
    val rotationDegrees: Float = 0f,
    val scale: Float = 1f,
    val z: Int = 0,
) : Component<Transform>, Comparable<Transform> {
    override fun type() = Transform

    override fun compareTo(other: Transform): Int {
        val zCmp = this.z.compareTo(other.z)
        if (zCmp != 0) return zCmp
        val yCmp = other.position.y.compareTo(this.position.y)
        if (yCmp != 0) return yCmp
        return this.position.x.compareTo(other.position.x)
    }

    companion object : ComponentType<Transform>()
}