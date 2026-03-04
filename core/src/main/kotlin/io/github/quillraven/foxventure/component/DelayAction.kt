package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class DelayAction(
    var delay: Float,
    val removeAfterAction: Boolean,
    val action: () -> Unit,
) : Component<DelayAction> {
    override fun type() = DelayAction

    companion object : ComponentType<DelayAction>()
}