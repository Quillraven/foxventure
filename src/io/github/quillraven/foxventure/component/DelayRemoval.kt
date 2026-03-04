package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class DelayRemoval(
    var timer: Float
) : Component<DelayRemoval> {
    override fun type() = DelayRemoval

    companion object : ComponentType<DelayRemoval>()
}
