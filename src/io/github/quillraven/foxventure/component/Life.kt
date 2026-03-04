package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Life(
    var amount: Int,
) : Component<Life> {
    override fun type() = Life

    companion object : ComponentType<Life>()
}