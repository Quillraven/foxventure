package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Flash(
    var duration: Float,
) : Component<Flash> {
    var timer: Float = 0f

    override fun type() = Flash

    companion object : ComponentType<Flash>()
}
