package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Links an entity to a Tiled map object by its [id].
 */
data class Tiled(val id: Int) : Component<Tiled> {
    override fun type() = Tiled

    companion object : ComponentType<Tiled>()
}