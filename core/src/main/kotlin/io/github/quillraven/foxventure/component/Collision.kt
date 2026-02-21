package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits

data class Box(val x: Float, val y: Float, val width: Float, val height: Float) {
    companion object {
        fun ofRect(rect: Rectangle) = Box(
            rect.x.toWorldUnits(), rect.y.toWorldUnits(),
            rect.width.toWorldUnits(), rect.height.toWorldUnits()
        )
    }
}

data class Collision(
    val box: Box,
    var isGrounded: Boolean = false,
) : Component<Collision> {
    override fun type() = Collision

    companion object : ComponentType<Collision>()
}