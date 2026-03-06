package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Life(
    var maxAmount: Int,
    var amount: Float = maxAmount.toFloat(),
) : Component<Life> {
    override fun type() = Life

    companion object : ComponentType<Life>()
}