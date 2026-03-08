package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Health component with [maxAmount] and current [amount]. Tracks pending [heal] value.
 */
data class Life(
    var maxAmount: Int,
    var amount: Float = maxAmount.toFloat(),
) : Component<Life> {
    var heal: Int = 0

    override fun type() = Life

    companion object : ComponentType<Life>()
}