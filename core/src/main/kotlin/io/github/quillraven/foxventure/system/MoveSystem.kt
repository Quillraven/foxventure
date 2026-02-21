package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.PhysicsConfig
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

class MoveSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Transform, Collision, PhysicsConfig, JumpControl) },
    interval = Fixed(1 / 60f),
) {
    private val tempRect = Rectangle()
    private val checkRect = Rectangle()

    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity]
        val collision = entity[Collision]
        val physics = entity[PhysicsConfig]
        val jumpControl = entity[JumpControl]

        // Remember the previous position for smooth rendering
        velocity.prevPosition.set(velocity.targetPosition)

        // Read input from the controller if available
        var inputX = 0f
        var inputY = 0f
        var jumpPressed = false
        entity.getOrNull(Controller)?.let { controller ->
            if (controller.isActive(Command.MOVE_LEFT)) inputX -= 1f
            if (controller.isActive(Command.MOVE_RIGHT)) inputX += 1f
            if (controller.isActive(Command.MOVE_UP)) inputY += 1f
            if (controller.isActive(Command.MOVE_DOWN)) inputY -= 1f
            jumpPressed = controller.isActive(Command.JUMP)
        }

        // Check for a nearby ladder (within X units tolerance)
        val ladderNearby = checkLadderNearby(velocity, collision)

        // Ladder attachment logic
        if (ladderNearby && inputY != 0f) {
            collision.isOnLadder = true
            // Snap to ladder center horizontally
            velocity.targetPosition.x =
                tempRect.x + tempRect.width * 0.5f - collision.box.width * 0.5f - collision.box.x
        }

        // Exit ladder when moving horizontally
        if (collision.isOnLadder && inputX != 0f) {
            collision.isOnLadder = false
        }

        val isGrounded = collision.isGrounded

        // Ladder climbing
        if (collision.isOnLadder) {
            velocity.current.x = 0f
            velocity.current.y = inputY * physics.climbSpeed

            // Jump from the ladder
            if (jumpPressed && inputY >= 0f) {
                collision.isOnLadder = false
                velocity.current.y = physics.jumpImpulse
                jumpControl.jumpInput = true
            }
        } else {
            // Normal horizontal movement with acceleration
            val accel = if (isGrounded) physics.acceleration else physics.acceleration * physics.airControl
            val decel = if (isGrounded) physics.deceleration else physics.deceleration * 0.5f

            if (inputX != 0f) {
                // Check if skidding (pressing an opposite direction)
                val isSkidding = sign(inputX) != sign(velocity.current.x) && velocity.current.x != 0f

                if (isSkidding) {
                    val reduction = physics.skidDeceleration * deltaTime
                    if (abs(velocity.current.x) <= reduction) {
                        velocity.current.x = 0f
                    } else {
                        velocity.current.x -= sign(velocity.current.x) * reduction
                    }
                } else {
                    velocity.current.x += inputX * accel * deltaTime
                    velocity.current.x = velocity.current.x.coerceIn(-physics.maxSpeed, physics.maxSpeed)
                }
            } else {
                val reduction = decel * deltaTime
                if (abs(velocity.current.x) <= reduction) {
                    velocity.current.x = 0f
                } else {
                    velocity.current.x -= sign(velocity.current.x) * reduction
                }
            }

            // handle jump
            jumpControl.coyoteTimer -= deltaTime
            jumpControl.jumpBufferTimer -= deltaTime

            if (isGrounded) {
                jumpControl.coyoteTimer = physics.coyoteThreshold
            }

            if (jumpPressed) {
                jumpControl.jumpBufferTimer = physics.jumpBufferThreshold
            }

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

            // Gravity with peak hang time
            val isAtPeak =
                abs(velocity.current.y) < physics.peakVelocityThreshold && !isGrounded && jumpControl.jumpInput
            val gravityMultiplier = if (isAtPeak) physics.peakGravityMultiplier else 1f
            velocity.current.y -= physics.gravity * gravityMultiplier * deltaTime
            velocity.current.y = velocity.current.y.coerceAtLeast(-physics.maxFallSpeed)
        }

        // Move and collide
        moveX(collision, velocity, velocity.current.x * deltaTime)
        moveY(collision, velocity, velocity.current.y * deltaTime)

        // Check if still on the ladder after movement
        if (collision.isOnLadder && !ladderNearby) {
            collision.isOnLadder = false
        }
    }

    private fun moveX(collision: Collision, velocity: Velocity, deltaX: Float) {
        if (deltaX == 0f) return

        velocity.targetPosition.x += deltaX

        // Clamp to map boundaries
        val minX = -collision.box.x
        val maxX = tiledService.mapWidth - collision.box.x - collision.box.width
        velocity.targetPosition.x = velocity.targetPosition.x.coerceIn(minX, maxX)

        updateCheckRect(velocity, collision)

        if (checkCollision(includeSemiSolid = false)) {
            if (deltaX > 0f) {
                velocity.targetPosition.x = tempRect.x - collision.box.x - collision.box.width
            } else {
                velocity.targetPosition.x = tempRect.x + tempRect.width - collision.box.x
            }
            velocity.current.x = 0f
        }
    }

    private fun moveY(collision: Collision, velocity: Velocity, deltaY: Float) {
        if (deltaY == 0f) return

        val prevBottom = velocity.targetPosition.y + collision.box.y
        velocity.targetPosition.y += deltaY
        updateCheckRect(velocity, collision)

        collision.isGrounded = false

        if (checkCollision(includeSemiSolid = false)) {
            if (deltaY > 0f) {
                // Ceiling collision - try corner correction
                val tolerance = 0.3f
                val originalX = velocity.targetPosition.x

                // Try moving right
                velocity.targetPosition.x = originalX + tolerance
                updateCheckRect(velocity, collision)
                if (!checkCollision(includeSemiSolid = false)) {
                    return // Successfully corrected to the right
                }

                // Try moving left
                velocity.targetPosition.x = originalX - tolerance
                updateCheckRect(velocity, collision)
                if (!checkCollision(includeSemiSolid = false)) {
                    return // Successfully corrected to the left
                }

                // No correction possible, revert and stop
                velocity.targetPosition.x = originalX
                velocity.targetPosition.y = tempRect.y - collision.box.y - collision.box.height
                velocity.current.y = 0f
            } else {
                velocity.targetPosition.y = tempRect.y + tempRect.height - collision.box.y
                velocity.current.y = 0f
                collision.isGrounded = true
            }
            return
        }

        // Check semisolid only if falling and was above the platform
        if (deltaY < 0f && checkCollision(includeSemiSolid = true)) {
            if (prevBottom >= tempRect.y + tempRect.height) {
                velocity.targetPosition.y = tempRect.y + tempRect.height - collision.box.y
                velocity.current.y = 0f
                collision.isGrounded = true
            }
        }
    }

    private fun updateCheckRect(velocity: Velocity, collision: Collision) {
        checkRect.set(
            velocity.targetPosition.x + collision.box.x,
            velocity.targetPosition.y + collision.box.y,
            collision.box.width,
            collision.box.height
        )
    }

    private fun checkCollision(includeSemiSolid: Boolean): Boolean {
        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getCollisionRect(x, y, includeSemiSolid, tempRect)
                if (tempRect.width > 0f && checkRect.overlaps(tempRect)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkLadderNearby(velocity: Velocity, collision: Collision): Boolean {
        val tolerance = 0.3f
        // Expand check area by tolerance
        checkRect.set(
            velocity.targetPosition.x + collision.box.x - tolerance,
            velocity.targetPosition.y + collision.box.y,
            collision.box.width + tolerance * 2f,
            collision.box.height
        )

        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getLadderRect(x, y, tempRect)
                if (tempRect.width > 0f && checkRect.overlaps(tempRect)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val (_, prevPosition, targetPosition) = entity[Velocity]
        entity[Transform].position.set(
            MathUtils.lerp(prevPosition.x, targetPosition.x, alpha),
            MathUtils.lerp(prevPosition.y, targetPosition.y, alpha),
        )
    }
}