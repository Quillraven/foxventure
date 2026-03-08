package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Makes an entity invulnerable for [duration] seconds.
 */
data class Invulnerable(
    var duration: Float,
) : Component<Invulnerable> {
    override fun type() = Invulnerable

    companion object : ComponentType<Invulnerable>()
}
