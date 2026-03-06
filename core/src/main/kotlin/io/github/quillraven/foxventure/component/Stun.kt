package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Stun(
    var duration: Float,
) : Component<Stun> {
    override fun type() = Stun

    companion object : ComponentType<Stun>()
}
