package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Physics(
    var gravity: Float,
    var maxFallSpeed: Float,
    var jumpImpulse: Float,
    var coyoteThreshold: Float,
    var jumpBufferThreshold: Float,
    var maxSpeed: Float,
    var acceleration: Float,
    var deceleration: Float,
    var skidDeceleration: Float,
    var airControl: Float,
    var peakGravityMultiplier: Float,
    var peakVelocityThreshold: Float,
    var climbSpeed: Float,
    val position: Vector2,
    val prevPosition: Vector2 = position.cpy(),
) : Component<Physics> {
    override fun type() = Physics

    companion object : ComponentType<Physics>()
}