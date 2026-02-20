package io.github.quillraven.foxventure.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.PhysicsConfig
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

class MoveSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Transform, Collision, PhysicsConfig, JumpControl) }
) {
    private val tempRect = Rectangle()
    private val checkRect = Rectangle()

    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity]
        val transform = entity[Transform]
        val collision = entity[Collision]
        val physics = entity[PhysicsConfig]
        val jumpControl = entity[JumpControl]

        // Input handling
        var inputX = 0f
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputX -= 1f
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputX += 1f
        val jumpPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE)

        // Horizontal movement with acceleration
        val accel = if (collision.isGrounded) physics.acceleration else physics.acceleration * physics.airControl
        val decel = if (collision.isGrounded) physics.deceleration else physics.deceleration * 0.5f

        if (inputX != 0f) {
            velocity.current.x += inputX * accel * deltaTime
            velocity.current.x = velocity.current.x.coerceIn(-physics.maxSpeed, physics.maxSpeed)
        } else {
            val reduction = decel * deltaTime
            if (abs(velocity.current.x) <= reduction) {
                velocity.current.x = 0f
            } else {
                velocity.current.x -= sign(velocity.current.x) * reduction
            }
        }

        // Jump handling
        jumpControl.coyoteTimer -= deltaTime
        jumpControl.jumpBufferTimer -= deltaTime

        if (collision.isGrounded) {
            jumpControl.coyoteTimer = physics.coyoteThreshold
        }

        if (jumpPressed) {
            jumpControl.jumpBufferTimer = physics.jumpBufferThreshold
        }

        if (jumpControl.jumpBufferTimer > 0f && jumpControl.coyoteTimer > 0f) {
            val speedBonus = abs(velocity.current.x) / physics.maxSpeed * 0.25f
            velocity.current.y = physics.jumpImpulse * (1f + speedBonus)
            jumpControl.jumpBufferTimer = 0f
            jumpControl.coyoteTimer = 0f
            jumpControl.isRequestingJump = true
        }

        // Variable jump height
        if (!jumpPressed && jumpControl.isRequestingJump && velocity.current.y > 0f) {
            velocity.current.y *= 0.4f
            jumpControl.isRequestingJump = false
        }

        // Gravity
        velocity.current.y -= physics.gravity * deltaTime
        velocity.current.y = velocity.current.y.coerceAtLeast(-physics.maxFallSpeed)

        // Move and collide
        moveX(transform, collision, velocity, velocity.current.x * deltaTime)
        moveY(transform, collision, velocity, velocity.current.y * deltaTime)
    }

    private fun moveX(transform: Transform, collision: Collision, velocity: Velocity, deltaX: Float) {
        if (deltaX == 0f) return

        transform.position.x += deltaX
        updateCheckRect(transform, collision)

        if (checkCollision(includeSemiSolid = false)) {
            if (deltaX > 0f) {
                transform.position.x = tempRect.x - collision.rect.x - collision.rect.width
            } else {
                transform.position.x = tempRect.x + tempRect.width - collision.rect.x
            }
            velocity.current.x = 0f
        }
    }

    private fun moveY(transform: Transform, collision: Collision, velocity: Velocity, deltaY: Float) {
        if (deltaY == 0f) return

        val prevBottom = transform.position.y + collision.rect.y
        transform.position.y += deltaY
        updateCheckRect(transform, collision)

        collision.isGrounded = false

        if (checkCollision(includeSemiSolid = false)) {
            if (deltaY > 0f) {
                transform.position.y = tempRect.y - collision.rect.y - collision.rect.height
                velocity.current.y = 0f
            } else {
                transform.position.y = tempRect.y + tempRect.height - collision.rect.y
                velocity.current.y = 0f
                collision.isGrounded = true
            }
            return
        }

        // Check semisolids only if falling and was above the platform
        if (deltaY < 0f && checkCollision(includeSemiSolid = true)) {
            if (prevBottom >= tempRect.y + tempRect.height) {
                transform.position.y = tempRect.y + tempRect.height - collision.rect.y
                velocity.current.y = 0f
                collision.isGrounded = true
            }
        }
    }

    private fun updateCheckRect(transform: Transform, collision: Collision) {
        checkRect.set(
            transform.position.x + collision.rect.x,
            transform.position.y + collision.rect.y,
            collision.rect.width,
            collision.rect.height
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
}