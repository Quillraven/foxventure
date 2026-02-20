package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class PhysicsConfig(
    var gravity: Float,
    var maxFallSpeed: Float,
    var jumpImpulse: Float,
    var coyoteThreshold: Float,
    var jumpBufferThreshold: Float,
    var maxSpeed: Float,
    var acceleration: Float,
    var deceleration: Float,
    var airControl: Float,
) : Component<PhysicsConfig> {
    override fun type() = PhysicsConfig

    companion object : ComponentType<PhysicsConfig>()
}