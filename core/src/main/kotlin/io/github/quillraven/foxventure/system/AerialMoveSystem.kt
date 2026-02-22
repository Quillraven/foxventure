package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command

class AerialMoveSystem(
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, JumpControl, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
) {
    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val collision = entity[Collision]
        val velocity = entity[Velocity]
        val physics = entity[Physics]
        val jumpControl = entity[JumpControl]

        val controller = entity.getOrNull(Controller)
        val jumpPressed = controller?.hasCommand(Command.JUMP) == true
        val isGrounded = collision.isGrounded

        updateJumpState(velocity, physics, jumpControl, jumpPressed, isGrounded, physicsTimer.interval)
        applyGravity(velocity, physics, jumpControl, isGrounded, physicsTimer.interval)
    }

    private fun updateJumpState(
        velocity: Velocity,
        physics: Physics,
        jumpControl: JumpControl,
        jumpPressed: Boolean,
        isGrounded: Boolean,
        deltaTime: Float
    ) {
        jumpControl.coyoteTimer -= deltaTime
        jumpControl.jumpBufferTimer -= deltaTime

        if (isGrounded) jumpControl.coyoteTimer = physics.coyoteThreshold
        
        // Only set buffer on jump press (not hold)
        if (jumpPressed && !jumpControl.wasJumpPressed) {
            jumpControl.jumpBufferTimer = physics.jumpBufferThreshold
        }
        jumpControl.wasJumpPressed = jumpPressed

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
        physics: Physics,
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
