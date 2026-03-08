package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Wander(
    val distance: Float,
    val originX: Float,
    val stopAtCliff: Boolean,
) : Component<Wander> {
    var moveDirection: Float = 0f

    override fun type() = Wander

    companion object : ComponentType<Wander>()
}