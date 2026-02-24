package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits

data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) {
    fun overlaps(otherX: Float, otherY: Float, otherWidth: Float, otherHeight: Float): Boolean {
        return x < otherX + otherWidth && x + width > otherX && y < otherY + otherHeight && y + height > otherY
    }

    fun overlaps(position: Vector2, otherPosition: Vector2, otherRect: Rect): Boolean {
        val thisX = x + position.x
        val thisY = y + position.y
        val otherX = otherPosition.x + otherRect.x
        val otherY = otherPosition.y + otherRect.y

        return thisX < otherX + otherRect.width && thisX + width > otherX && thisY < otherY + otherRect.height && thisY + height > otherY
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