package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Collision(
    val rect: Rectangle,
    var isGrounded: Boolean = false,
) : Component<Collision> {
    override fun type() = Collision

    companion object : ComponentType<Collision>()
}