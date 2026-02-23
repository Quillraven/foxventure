package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits

data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) {
    fun overlaps(rect: Rectangle): Boolean {
        return x < rect.x + rect.width && x + width > rect.x && y < rect.y + rect.height && y + height > rect.y
    }

    companion object {
        fun ofRect(rect: Rectangle) = Rect(
            rect.x.toWorldUnits(), rect.y.toWorldUnits(),
            rect.width.toWorldUnits(), rect.height.toWorldUnits()
        )

        fun Rectangle.set(box: Rect) {
            this.set(box.x, box.y, box.width, box.height)
        }
    }
}

data class Collision(
    val box: Rect,
    var isGrounded: Boolean = false,
    var isOnLadder: Boolean = false,
) : Component<Collision> {
    override fun type() = Collision

    companion object : ComponentType<Collision>()
}