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
import io.github.quillraven.foxventure.component.Physics
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
    private val tileRect = Rectangle()
    private val checkRect = Rectangle()

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

    /**
     * Updates the x-velocity of an entity based on input, physics properties, and environmental factors.
     *
     * @param velocity The current velocity vector of the entity, which will be modified in place.
     * @param physics The physics configuration containing properties like acceleration, deceleration, and max speed.
     * @param inputX The horizontal input value. Typically ranges between -1.0 and 1.0, representing directional input.
     * @param isGrounded A flag indicating whether the entity is currently grounded (true) or airborne (false).
     * @param deltaTime The time that has passed since the last update, used for framerate-independent adjustments.
     */
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
                val rate = if (isSkidding) physics.skidDeceleration else acceleration

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
        collisionBox: Box,
    ) {
        val delta = velocity.x * physicsTimer.interval
        if (delta == 0f) return

        position.x += delta
        clampToMapBounds(position, collisionBox)

        if (checkSolidCollision(position, collisionBox)) {
            // colliding with a solid -> move to edge of solid and stop movement
            position.x = when {
                delta > 0f -> tileRect.x - collisionBox.x - collisionBox.width
                else -> tileRect.x + tileRect.width - collisionBox.x
            }
            velocity.x = 0f
        }
    }

    private fun clampToMapBounds(position: Vector2, collisionBox: Box) {
        val minX = -collisionBox.x
        val maxX = tiledService.mapWidth - collisionBox.x - collisionBox.width
        position.x = position.x.coerceIn(minX, maxX)
    }

    private fun checkSolidCollision(position: Vector2, collisionBox: Box): Boolean {
        checkRect.set(
            position.x + collisionBox.x,
            position.y + collisionBox.y,
            collisionBox.width,
            collisionBox.height
        )

        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getCollisionRect(x, y, includeSemiSolid = false, tileRect)
                if (tileRect.width > 0f && checkRect.overlaps(tileRect)) {
                    return true
                }
            }
        }
        return false
    }
}
