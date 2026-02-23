package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
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
        val velocity = entity[Velocity]
        val collision = entity[Collision]
        val physics = entity[Physics]

        val controller = entity.getOrNull(Controller)
        val inputX = getInputX(controller)

        updateHorizontalVelocity(velocity, physics, inputX, collision.isGrounded, physicsTimer.interval)
        applyHorizontalMovement(collision, physics, velocity)
    }

    private fun getInputX(controller: Controller?): Float {
        if (controller == null) return 0f
        var input = 0f
        if (controller.hasCommand(Command.MOVE_LEFT)) input -= 1f
        if (controller.hasCommand(Command.MOVE_RIGHT)) input += 1f
        return input
    }

    private fun updateHorizontalVelocity(
        velocity: Velocity,
        physics: Physics,
        inputX: Float,
        isGrounded: Boolean,
        deltaTime: Float
    ) {
        val accel = if (isGrounded) physics.acceleration else physics.acceleration * physics.airControl
        val decel = if (isGrounded) physics.deceleration else physics.deceleration * 0.5f

        when {
            inputX != 0f -> {
                val isSkidding = sign(inputX) != sign(velocity.current.x) && velocity.current.x != 0f
                val rate = if (isSkidding) physics.skidDeceleration else accel

                velocity.current.x = if (isSkidding) {
                    moveTowards(velocity.current.x, rate * deltaTime)
                } else {
                    (velocity.current.x + inputX * rate * deltaTime).coerceIn(-physics.maxSpeed, physics.maxSpeed)
                }
            }

            else -> velocity.current.x = moveTowards(velocity.current.x, decel * deltaTime)
        }
    }

    private fun moveTowards(current: Float, maxDelta: Float): Float {
        val diff = -current
        return if (abs(diff) <= maxDelta) 0f else current + sign(diff) * maxDelta
    }

    private fun applyHorizontalMovement(collision: Collision, physics: Physics, velocity: Velocity) {
        val delta = velocity.current.x * physicsTimer.interval
        if (delta == 0f) return

        physics.position.x += delta
        clampToMapBounds(physics, collision)
        updateCheckRect(physics, collision)

        if (checkTileCollision()) {
            physics.position.x = if (delta > 0f) {
                tileRect.x - collision.box.x - collision.box.width
            } else {
                tileRect.x + tileRect.width - collision.box.x
            }
            velocity.current.x = 0f
        }
    }

    private fun clampToMapBounds(physics: Physics, collision: Collision) {
        val minX = -collision.box.x
        val maxX = tiledService.mapWidth - collision.box.x - collision.box.width
        physics.position.x = physics.position.x.coerceIn(minX, maxX)
    }

    private fun updateCheckRect(physics: Physics, collision: Collision) {
        checkRect.set(
            physics.position.x + collision.box.x,
            physics.position.y + collision.box.y,
            collision.box.width,
            collision.box.height
        )
    }

    private fun checkTileCollision(): Boolean {
        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getCollisionRect(x, y, false, tileRect)
                if (tileRect.width > 0f && checkRect.overlaps(tileRect)) return true
            }
        }
        return false
    }
}
