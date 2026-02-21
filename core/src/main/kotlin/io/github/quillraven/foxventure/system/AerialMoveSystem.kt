package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.PhysicsConfig
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command

class AerialMoveSystem : IteratingSystem(
    family = family { all(Velocity, Collision, PhysicsConfig, JumpControl, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
    interval = Fixed(1 / 60f),
) {
    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity]
        val collision = entity[Collision]
        val physics = entity[PhysicsConfig]
        val jumpControl = entity[JumpControl]

        val controller = entity.getOrNull(Controller)
        val jumpPressed = controller?.hasCommand(Command.JUMP) == true
        val isGrounded = collision.isGrounded

        updateJumpState(velocity, physics, jumpControl, jumpPressed, isGrounded, deltaTime)
        applyGravity(velocity, physics, jumpControl, isGrounded, deltaTime)
    }

    private fun updateJumpState(
        velocity: Velocity,
        physics: PhysicsConfig,
        jumpControl: JumpControl,
        jumpPressed: Boolean,
        isGrounded: Boolean,
        deltaTime: Float
    ) {
        jumpControl.coyoteTimer -= deltaTime
        jumpControl.jumpBufferTimer -= deltaTime

        if (isGrounded) jumpControl.coyoteTimer = physics.coyoteThreshold
        if (jumpPressed) jumpControl.jumpBufferTimer = physics.jumpBufferThreshold

        // Execute jump
        if (jumpControl.jumpBufferTimer > 0f && jumpControl.coyoteTimer > 0f) {
            velocity.current.y = physics.jumpImpulse
            jumpControl.jumpBufferTimer = 0f
            jumpControl.coyoteTimer = 0f
            jumpControl.jumpInput = true
        }

        // Variable jump height
        if (!jumpPressed && jumpControl.jumpInput && velocity.current.y > 0f) {
            velocity.current.y *= 0.4f
            jumpControl.jumpInput = false
        }
    }

    private fun applyGravity(
        velocity: Velocity,
        physics: PhysicsConfig,
        jumpControl: JumpControl,
        isGrounded: Boolean,
        deltaTime: Float
    ) {
        val isAtPeak =
            kotlin.math.abs(velocity.current.y) < physics.peakVelocityThreshold && !isGrounded && jumpControl.jumpInput
        val gravityMultiplier = if (isAtPeak) physics.peakGravityMultiplier else 1f

        velocity.current.y = (velocity.current.y - physics.gravity * gravityMultiplier * deltaTime)
            .coerceAtLeast(-physics.maxFallSpeed)
    }
}
