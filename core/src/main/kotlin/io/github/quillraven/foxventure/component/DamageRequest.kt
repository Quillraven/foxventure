package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

/**
 * Request to create a damage area from [source] with [damage], [position], [size], and [lifeSpan].
 */
data class DamageRequest(
    val source: Entity,
    val damage: Int,
    val position: Vector2,
    val size: Vector2,
    val lifeSpan: Float,
) : Component<DamageRequest> {
    override fun type() = DamageRequest

    companion object : ComponentType<DamageRequest>()
}