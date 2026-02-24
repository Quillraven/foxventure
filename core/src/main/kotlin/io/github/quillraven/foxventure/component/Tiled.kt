package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Tiled(
    val id: Int,
    val type: String,
) : Component<Tiled> {
    override fun type() = Tiled

    companion object : ComponentType<Tiled>()
}