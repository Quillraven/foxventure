package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Attack capability with [range], [cooldown], and [damage]. Tracks [time] until next attack and [readyToAttack] state.
 */
data class Attack(
    val range: Float,
    val cooldown: Float,
    val damage: Int,
) : Component<Attack> {
    var time: Float = 0f
    var readyToAttack: Boolean = false

    override fun type() = Attack

    fun resetCooldown() {
        time = cooldown
        readyToAttack = false
    }

    companion object : ComponentType<Attack>()
}
