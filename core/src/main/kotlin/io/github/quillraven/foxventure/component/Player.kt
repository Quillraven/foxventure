package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Player(
    var health: Float,
) : Component<Player> {
    override fun type() = Player

    companion object : ComponentType<Player>()
}