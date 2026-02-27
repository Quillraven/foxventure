package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Wander(
    val distance: Float,
    val originX: Float,
    val stopAtCliff: Boolean,
    var moveDirection: Float = 0f,
) : Component<Wander> {
    override fun type() = Wander

    companion object : ComponentType<Wander>()
}