package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

class GroundMoveSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
) {
    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity].current
        val collision = entity[Collision]
        val physics = entity[Physics]

        val controller = entity.getOrNull(Controller)
        val inputX = getInputX(controller)

        updateHorizontalVelocity(velocity, physics, inputX, collision.isGrounded, physicsTimer.interval)
        applyHorizontalMovement(velocity, physics.position, collision.box)
    }

    private fun getInputX(controller: Controller?): Float {
        if (controller == null) return 0f
        var input = 0f
        if (controller.hasCommand(Command.MOVE_LEFT)) input -= 1f
        if (controller.hasCommand(Command.MOVE_RIGHT)) input += 1f
        return input
    }

    private fun sCurveAcceleration(speedPercent: Float): Float {
        val t = speedPercent.coerceIn(0.25f, 1f)
        val smoothed = t * t * (3f - 2f * t)
        return 0.5f + smoothed * 0.5f
    }

    private fun updateHorizontalVelocity(
        velocity: Vector2,
        physics: Physics,
        inputX: Float,
        isGrounded: Boolean,
        deltaTime: Float
    ) {
        when {
            inputX != 0f -> {
                val acceleration = if (isGrounded) physics.acceleration else physics.acceleration * physics.airControl
                val isSkidding = velocity.x != 0f && sign(inputX) != sign(velocity.x)
                val rate = if (isSkidding) {
                    physics.skidDeceleration
                } else {
                    val speedPercent = abs(velocity.x) / physics.maxSpeed
                    acceleration * sCurveAcceleration(speedPercent)
                }

                velocity.x = when {
                    isSkidding -> adjustToZero(velocity.x, rate * deltaTime)
                    else -> (velocity.x + inputX * rate * deltaTime).coerceIn(-physics.maxSpeed, physics.maxSpeed)
                }
            }

            else -> {
                val deceleration = if (isGrounded) physics.deceleration else physics.deceleration * physics.airControl
                velocity.x = adjustToZero(velocity.x, deceleration * deltaTime)
            }
        }
    }

    /**
     * Adjusts the current value to move closer to zero by a specified maximum delta.
     * If the remaining difference to zero is less than or equal to the maximum delta,
     * the result will be exactly zero. Otherwise, the value will be adjusted by the
     * maximum delta in the direction towards zero.
     *
     * @param current The current value to be adjusted.
     * @param maxDelta The maximum change allowed towards zero.
     * @return The new adjusted value, closer to zero by at most the specified maximum delta.
     */
    private fun adjustToZero(current: Float, maxDelta: Float): Float {
        val diff = -current
        return if (abs(diff) <= maxDelta) 0f else current + sign(diff) * maxDelta
    }

    private fun applyHorizontalMovement(
        velocity: Vector2,
        position: Vector2,
        collisionBox: Rect,
    ) {
        val delta = velocity.x * physicsTimer.interval
        if (delta == 0f) return

        position.x += delta
        clampToMapBounds(position, collisionBox)

        val rect = tiledService.getCollisionRect(position, collisionBox, includeSemiSolid = false)
        if (rect != null) {
            // colliding with a solid -> move to edge of solid and stop movement
            position.x = when {
                delta > 0f -> rect.x - collisionBox.x - collisionBox.width
                else -> rect.x + rect.width - collisionBox.x
            }
            velocity.x = 0f
        }
    }

    private fun clampToMapBounds(position: Vector2, collisionBox: Rect) {
        val minX = -collisionBox.x
        val maxX = tiledService.mapWidth - collisionBox.x - collisionBox.width
        position.x = position.x.coerceIn(minX, maxX)
    }
}
