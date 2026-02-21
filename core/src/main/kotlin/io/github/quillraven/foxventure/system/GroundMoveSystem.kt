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
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.PhysicsConfig
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

class GroundMoveSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Transform, Collision, PhysicsConfig, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
    interval = Fixed(1 / 60f),
) {
    private val tileRect = Rectangle()
    private val checkRect = Rectangle()

    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity]
        val collision = entity[Collision]
        val physics = entity[PhysicsConfig]

        velocity.prevPosition.set(velocity.targetPosition)

        val controller = entity.getOrNull(Controller)
        val inputX = getInputX(controller)
        val downPressed = controller?.hasCommand(Command.MOVE_DOWN) == true

        updateHorizontalVelocity(velocity, physics, inputX, collision.isGrounded, deltaTime)
        applyMovement(collision, velocity, downPressed)

        if (velocity.current.x != 0f) {
            entity.getOrNull(Graphic)?.let { it.flip = velocity.current.x < 0f }
        }
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
        physics: PhysicsConfig,
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

    private fun applyMovement(collision: Collision, velocity: Velocity, downPressed: Boolean) {
        moveAxis(collision, velocity, velocity.current.x * deltaTime, isVertical = false)
        moveAxis(collision, velocity, velocity.current.y * deltaTime, isVertical = true, downPressed)
    }

    private fun moveAxis(
        collision: Collision,
        velocity: Velocity,
        delta: Float,
        isVertical: Boolean,
        downPressed: Boolean = false
    ) {
        if (delta == 0f) return

        if (isVertical) {
            val prevBottom = velocity.targetPosition.y + collision.box.y
            velocity.targetPosition.y += delta
            updateCheckRect(velocity, collision)
            collision.isGrounded = false

            handleVerticalCollision(collision, velocity, delta, prevBottom, downPressed)
        } else {
            velocity.targetPosition.x += delta
            clampToMapBounds(velocity, collision)
            updateCheckRect(velocity, collision)

            if (checkTileCollision(includeSemiSolid = false)) {
                velocity.targetPosition.x = if (delta > 0f) {
                    tileRect.x - collision.box.x - collision.box.width
                } else {
                    tileRect.x + tileRect.width - collision.box.x
                }
                velocity.current.x = 0f
            }
        }
    }

    private fun clampToMapBounds(velocity: Velocity, collision: Collision) {
        val minX = -collision.box.x
        val maxX = tiledService.mapWidth - collision.box.x - collision.box.width
        velocity.targetPosition.x = velocity.targetPosition.x.coerceIn(minX, maxX)
    }

    private fun handleVerticalCollision(
        collision: Collision,
        velocity: Velocity,
        delta: Float,
        prevBottom: Float,
        downPressed: Boolean
    ) {
        // Solid collision
        if (checkTileCollision(includeSemiSolid = false)) {
            if (delta > 0f) {
                if (!tryCeilingCorrection(collision, velocity)) {
                    velocity.targetPosition.y = tileRect.y - collision.box.y - collision.box.height
                    velocity.current.y = 0f
                }
            } else {
                velocity.targetPosition.y = tileRect.y + tileRect.height - collision.box.y
                velocity.current.y = 0f
                collision.isGrounded = true
            }
            return
        }

        // Semisolid and ladder collision (only when falling)
        if (delta >= 0f) return

        if (checkTileCollision(includeSemiSolid = true) && prevBottom >= tileRect.y + tileRect.height) {
            velocity.targetPosition.y = tileRect.y + tileRect.height - collision.box.y
            velocity.current.y = 0f
            collision.isGrounded = true
            return
        }

        // Top ladder tile collision
        if (!downPressed) {
            checkTopLadderCollision(collision, velocity, prevBottom)
        }
    }

    private fun tryCeilingCorrection(collision: Collision, velocity: Velocity): Boolean {
        val tolerance = 0.3f
        val originalX = velocity.targetPosition.x

        for (offset in listOf(tolerance, -tolerance)) {
            velocity.targetPosition.x = originalX + offset
            updateCheckRect(velocity, collision)
            if (!checkTileCollision(includeSemiSolid = false)) return true
        }

        velocity.targetPosition.x = originalX
        return false
    }

    private fun checkTopLadderCollision(collision: Collision, velocity: Velocity, prevBottom: Float) {
        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getLadderRect(x, y, tileRect)
                if (tileRect.width > 0f && checkRect.overlaps(tileRect) &&
                    tiledService.isTopLadderTile(x, y) && prevBottom >= tileRect.y + tileRect.height
                ) {
                    velocity.targetPosition.y = tileRect.y + tileRect.height - collision.box.y
                    velocity.current.y = 0f
                    collision.isGrounded = true
                    return
                }
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

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val (_, prevPosition, targetPosition) = entity[Velocity]
        entity[Transform].position.set(
            MathUtils.lerp(prevPosition.x, targetPosition.x, alpha),
            MathUtils.lerp(prevPosition.y, targetPosition.y, alpha),
        )
    }
}
