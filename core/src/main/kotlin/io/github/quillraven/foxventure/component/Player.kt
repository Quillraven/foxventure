package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Player(
    var credits: Int = 5,
    var gems: Int = 0,
) : Component<Player> {
    override fun type() = Player

    companion object : ComponentType<Player>()
}