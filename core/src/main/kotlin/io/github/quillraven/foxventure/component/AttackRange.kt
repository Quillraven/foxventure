package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AttackRange(
    val range: Float,
    var cooldown: Float = 0f,
) : Component<AttackRange> {
    override fun type() = AttackRange

    companion object : ComponentType<AttackRange>()
}
