package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Rectangle
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
import io.github.quillraven.foxventure.tiled.TiledService

class AerialMoveSystem(
    private val physicsTimer: PhysicsTimer = inject(),
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, JumpControl, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
) {
    private val tileRect = Rectangle()
    private val checkRect = Rectangle()

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
        applyVerticalMovement(collision, velocity, physics)
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

    private fun applyVerticalMovement(
        collision: Collision,
        velocity: Velocity,
        physics: Physics,
    ) {
        val delta = velocity.current.y * physicsTimer.interval
        if (delta == 0f) return

        val prevBottom = physics.position.y + collision.box.y
        physics.position.y += delta
        updateCheckRect(physics, collision)
        collision.isGrounded = false

        handleVerticalCollision(collision, velocity, physics, delta, prevBottom)
    }

    private fun handleVerticalCollision(
        collision: Collision,
        velocity: Velocity,
        physics: Physics,
        delta: Float,
        prevBottom: Float,
    ) {
        // Solid collision
        if (checkTileCollision(includeSemiSolid = false)) {
            if (delta > 0f) {
                // Ceiling collision - find the closest (highest Y) tile
                val ceilingY = findClosestCeilingTile()
                if (ceilingY != null) {
                    if (!tryCeilingCorrection(collision, physics)) {
                        physics.position.y = ceilingY - collision.box.y - collision.box.height
                        velocity.current.y = 0f
                    }
                }
            } else {
                physics.position.y = tileRect.y + tileRect.height - collision.box.y
                velocity.current.y = 0f
                collision.isGrounded = true
            }
            return
        }

        // Semisolid and ladder collision (only when falling)
        if (delta >= 0f) return

        if ((checkTileCollision(includeSemiSolid = true) || checkTopLadderCollision()) && prevBottom >= tileRect.y + tileRect.height) {
            physics.position.y = tileRect.y + tileRect.height - collision.box.y
            velocity.current.y = 0f
            collision.isGrounded = true
            return
        }
    }

    private fun tryCeilingCorrection(collision: Collision, physics: Physics): Boolean {
        val tolerance = 0.3f
        val originalX = physics.position.x

        for (offset in listOf(tolerance, -tolerance)) {
            physics.position.x = originalX + offset
            updateCheckRect(physics, collision)
            if (!checkTileCollision(includeSemiSolid = false)) return true
        }

        physics.position.x = originalX
        return false
    }

    private fun updateCheckRect(physics: Physics, collision: Collision) {
        checkRect.set(
            physics.position.x + collision.box.x,
            physics.position.y + collision.box.y,
            collision.box.width,
            collision.box.height
        )
    }

    private fun checkTileCollision(includeSemiSolid: Boolean): Boolean {
        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getCollisionRect(x, y, includeSemiSolid, tileRect)
                if (tileRect.width > 0f && checkRect.overlaps(tileRect)) return true
            }
        }
        return false
    }

    private fun checkTopLadderCollision(): Boolean {
        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                if (tiledService.isTopLadderTile(x, y)) {
                    tileRect.set(x.toFloat(), y.toFloat(), 1f, 1f)
                    if (checkRect.overlaps(tileRect)) return true
                }
            }
        }
        return false
    }

    private fun findClosestCeilingTile(): Float? {
        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        var closestY: Float? = null
        val tempRect = Rectangle()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getCollisionRect(x, y, false, tempRect)
                if (tempRect.width > 0f && checkRect.overlaps(tempRect)) {
                    val tileBottom = tempRect.y
                    if (closestY == null || tileBottom > closestY) {
                        closestY = tileBottom
                        tileRect.set(tempRect)
                    }
                }
            }
        }
        return closestY
    }
}
