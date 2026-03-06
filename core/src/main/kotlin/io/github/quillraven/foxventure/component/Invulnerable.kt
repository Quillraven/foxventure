package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Invulnerable(
    var duration: Float,
) : Component<Invulnerable> {
    override fun type() = Invulnerable

    companion object : ComponentType<Invulnerable>()
}
