package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Box
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs

class AerialMoveSystem(
    private val physicsTimer: PhysicsTimer = inject(),
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, JumpControl, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
) {
    private val solidRect = Rectangle()
    private val semiSolidRect = Rectangle()
    private val topLadderRect = Rectangle()
    private val ceilingRect = Rectangle()
    private val checkRect = Rectangle()

    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val collision = entity[Collision]
        val velocity = entity[Velocity].current
        val physics = entity[Physics]
        val jumpControl = entity[JumpControl]
        val controller = entity.getOrNull(Controller)
        val jumpPressed = controller?.hasCommand(Command.JUMP) == true

        updateJumpState(velocity, physics, jumpControl, jumpPressed, collision.isGrounded)
        applyGravity(velocity, physics, jumpControl.isJumping, collision.isGrounded)
        applyVerticalMovement(physics.position, collision, velocity)
    }

    private fun updateJumpState(
        velocity: Vector2,
        physics: Physics,
        jumpControl: JumpControl,
        jumpPressed: Boolean,
        isGrounded: Boolean,
    ) {
        if (checkForJumpStart(velocity, physics, jumpControl, jumpPressed, isGrounded)) {
            return
        }

        // check if the current jump should be aborted by releasing the jump button early
        if (!jumpPressed && jumpControl.isJumping && velocity.y > 0f) {
            velocity.y *= 0.4f
            jumpControl.isJumping = false
        }
    }

    private fun checkForJumpStart(
        velocity: Vector2,
        physics: Physics,
        jumpControl: JumpControl,
        jumpPressed: Boolean,
        isGrounded: Boolean
    ): Boolean {
        // update coyote timer (time to allow jump even when not grounded)
        if (isGrounded) {
            jumpControl.coyoteTimer = physics.coyoteThreshold
        } else if (jumpControl.coyoteTimer > 0f) {
            jumpControl.coyoteTimer -= physicsTimer.interval
        }

        // update jump buffer timer (time to check for valid jump condition after JUMP is pressed)
        if (jumpPressed && !jumpControl.isJumpCommandHeld) {
            // jump was really pressed and not just held down
            jumpControl.jumpBufferTimer = physics.jumpBufferThreshold
        } else if (jumpControl.jumpBufferTimer > 0f) {
            jumpControl.jumpBufferTimer -= physicsTimer.interval
        }
        jumpControl.isJumpCommandHeld = jumpPressed

        // if valid jump buffer and coyote time, then start jump
        if (jumpControl.jumpBufferTimer > 0f && jumpControl.coyoteTimer > 0f) {
            velocity.y = physics.jumpImpulse
            jumpControl.jumpBufferTimer = 0f
            jumpControl.coyoteTimer = 0f
            jumpControl.isJumping = true
            return true
        }
        return false
    }

    /**
     * Applies gravitational force to the vertical velocity of an entity, considering various physics parameters
     * and the state of the jump. This method adjusts the entity's y-velocity to simulate realistic gravity effects,
     * including peak gravity reduction for a "floaty" experience at the top of a jump and fall speed limitation.
     *
     * @param velocity The current velocity of the entity, which will be modified in this method.
     * @param physics The physics configuration containing parameters like gravity, maximum fall speed, and peak gravity multiplier.
     * @param isJumping A boolean indicating whether the entity is currently jumping.
     * @param isGrounded A boolean indicating whether the entity is currently on the ground.
     */
    private fun applyGravity(
        velocity: Vector2,
        physics: Physics,
        isJumping: Boolean,
        isGrounded: Boolean,
    ) {
        // velocity.y is high when the jump starts (=initial impulse) and then slows down over time
        // due to gravity until it is zero. Then the fall starts (=velocity.y < 0).
        // When close to the turning point and the jump is not aborted (=jumpControl.isJumping), then
        // apply lower gravity at the peak to give a more "floaty" experience at the top of the jump.
        val isAtPeak = abs(velocity.y) < physics.peakVelocityThreshold && !isGrounded && isJumping
        val gravityMultiplier = if (isAtPeak) physics.peakGravityMultiplier else 1f

        // cap fall speed according to physics configuration (=maxFallSpeed)
        velocity.y = (velocity.y - physics.gravity * gravityMultiplier * physicsTimer.interval)
            .coerceAtLeast(-physics.maxFallSpeed)
    }

    private fun applyVerticalMovement(
        position: Vector2,
        collision: Collision,
        velocity: Vector2,
    ) {
        val delta = velocity.y * physicsTimer.interval
        if (delta == 0f) {
            // no vertical movement -> do nothing
            return
        }

        collision.isGrounded = false
        val prevPositionY = position.y + collision.box.y
        position.y += delta
        if (!findCollidingTiles(position, collision.box)) {
            // no colliding tiles -> nothing to do
            return
        }

        if (handleSolidCollision(position, collision, velocity, delta)) {
            return
        }
        handleSemiSolidCollision(position, collision, velocity, delta, prevPositionY)
    }

    private fun findCollidingTiles(position: Vector2, collisionBox: Box): Boolean {
        updateCheckRect(position, collisionBox)
        return tiledService.getAllCollisionRects(
            checkRect,
            solidRect,
            semiSolidRect,
            topLadderRect
        )
    }

    private fun handleSolidCollision(
        position: Vector2,
        collision: Collision,
        velocity: Vector2,
        delta: Float,
    ): Boolean {
        if (solidRect.width == 0f) {
            // no solid collision -> do nothing
            return false
        }

        if (delta > 0f) {
            // ceiling collision
            tryCeilingCorrection(position, collision.box)
            position.y = solidRect.y - collision.box.y - collision.box.height
            velocity.y = 0f
            return true
        }

        // ground collision
        position.y = solidRect.y + solidRect.height - collision.box.y
        velocity.y = 0f
        collision.isGrounded = true
        return true
    }

    private fun handleSemiSolidCollision(
        position: Vector2,
        collision: Collision,
        velocity: Vector2,
        delta: Float,
        prevPositionY: Float,
    ) {
        if (delta > 0f || (semiSolidRect.width == 0f && topLadderRect.width == 0f)) {
            // no semisolid collision during jump or no semisolid/ladder collision
            return
        }

        val tileRect = when {
            semiSolidRect.y + semiSolidRect.height > topLadderRect.y + topLadderRect.height -> semiSolidRect
            else -> topLadderRect
        }

        if (prevPositionY < tileRect.y + tileRect.height) {
            // the previous position was below semisolid -> fall through it
            return
        }

        // the previous position was above semisolid -> attach to the top edge of it
        position.y = tileRect.y + tileRect.height - collision.box.y
        velocity.y = 0f
        collision.isGrounded = true
    }

    /**
     * Attempts to adjust the horizontal position of an entity to correct for potential collisions detected at the ceiling.
     * This method applies a small offset in both positive and negative horizontal directions
     * and checks for collisions to determine if a valid adjustment is possible.
     * It is used to push an entity outside a ceiling if it is close to the end of a ceiling.
     *
     * @param collisionBox The bounding box representing the entity's collision area.
     * @param position The current position of the entity, which may be updated during the correction process.
     * @return `true` if a valid correction was applied to avoid a ceiling collision, otherwise `false`.
     */
    private fun tryCeilingCorrection(position: Vector2, collisionBox: Box) {
        val tolerance = 0.3f
        val originalX = position.x

        fun hasNoCeilingCollision(checkPosition: Vector2): Boolean {
            updateCheckRect(checkPosition, collisionBox)
            val startX = checkRect.x.toInt()
            val endX = (checkRect.x + checkRect.width).toInt()
            val startY = checkRect.y.toInt()
            val endY = (checkRect.y + checkRect.height).toInt()

            for (y in startY..endY) {
                for (x in startX..endX) {
                    tiledService.getCollisionRect(x, y, includeSemiSolid = false, ceilingRect)
                    if (ceilingRect.width > 0f && checkRect.overlaps(ceilingRect)) {
                        return false
                    }
                }
            }
            return true
        }

        // 1) try on the right side
        position.x = originalX + tolerance
        if (hasNoCeilingCollision(position)) {
            return
        }

        // 2) try on the left side
        position.x = originalX - tolerance
        if (hasNoCeilingCollision(position)) {
            return
        }

        position.x = originalX
    }

    private fun updateCheckRect(position: Vector2, collisionBox: Box) {
        checkRect.set(
            position.x + collisionBox.x,
            position.y + collisionBox.y,
            collisionBox.width,
            collisionBox.height
        )
    }
}
