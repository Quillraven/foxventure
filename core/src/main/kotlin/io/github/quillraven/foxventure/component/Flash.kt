package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Flash(
    val duration: Float,
    var timer: Float = 0f,
) : Component<Flash> {
    override fun type() = Flash

    companion object : ComponentType<Flash>()
}
