package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.math.vec2

data class Velocity(
    val current: Vector2 = vec2(),
) : Component<Velocity> {
    override fun type() = Velocity

    companion object : ComponentType<Velocity>()
}