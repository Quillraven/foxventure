package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Attack(
    val range: Float,
    val cooldown: Float,
    val damage: Int,
    var time: Float = 0f,
    var readyToAttack: Boolean = false,
) : Component<Attack> {
    override fun type() = Attack

    fun resetCooldown() {
        time = cooldown
        readyToAttack = false
    }

    companion object : ComponentType<Attack>()
}
