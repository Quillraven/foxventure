package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Physics properties including [gravity], [maxFallSpeed], [jumpImpulse], jump timing thresholds,
 * movement speeds, [acceleration], [deceleration], [airControl], peak gravity adjustments,
 * [climbSpeed], and current [position] with [prevPosition] for interpolation.
 */
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

    companion object : ComponentType<Physics>() {
        fun projectilePhysics(position: Vector2, speed: Float, acceleration: Float = speed * 10f): Physics = Physics(
            gravity = 0f,
            maxFallSpeed = 0f,
            jumpImpulse = 0f,
            coyoteThreshold = 0f,
            jumpBufferThreshold = 0f,
            maxSpeed = speed,
            acceleration = acceleration,
            deceleration = 0f,
            skidDeceleration = 0f,
            airControl = 0f,
            peakGravityMultiplier = 0f,
            peakVelocityThreshold = 0f,
            climbSpeed = 0f,
            position = position,
        )
    }
}