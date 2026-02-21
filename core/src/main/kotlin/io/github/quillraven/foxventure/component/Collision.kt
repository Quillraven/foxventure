package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Box(val x: Float, val y: Float, val width: Float, val height: Float)

data class Collision(
    val box: Box,
    var isGrounded: Boolean = false,
) : Component<Collision> {
    override fun type() = Collision

    companion object : ComponentType<Collision>()
}