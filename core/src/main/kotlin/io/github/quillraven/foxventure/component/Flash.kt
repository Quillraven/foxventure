package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Flash(
    var duration: Float
) : Component<Flash> {
    override fun type() = Flash

    companion object : ComponentType<Flash>()
}
