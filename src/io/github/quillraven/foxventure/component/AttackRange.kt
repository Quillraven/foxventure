package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AttackRange(
    val range: Float,
    val cooldown: Float,
    var time: Float = 0f
) : Component<AttackRange> {
    override fun type() = AttackRange

    fun resetCooldown() {
        time = cooldown
    }

    companion object : ComponentType<AttackRange>()
}
