package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.math.vec2

/**
 * Tracks the [current] velocity of an entity and whether it [isSkidding] during movement.
 */
class Velocity : Component<Velocity> {
    val current: Vector2 = vec2()
    var isSkidding: Boolean = false

    override fun type() = Velocity

    companion object : ComponentType<Velocity>()
}