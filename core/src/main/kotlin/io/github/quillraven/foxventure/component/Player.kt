package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Player-specific data tracking [credits] and [gems] collected.
 */
data class Player(
    var credits: Int,
    var gems: Int,
) : Component<Player> {
    override fun type() = Player

    companion object : ComponentType<Player>()
}