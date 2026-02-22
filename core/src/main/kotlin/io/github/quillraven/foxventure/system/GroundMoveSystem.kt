package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs
import kotlin.math.sign

class GroundMoveSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Transform, Collision, Physics, EntityTag.ACTIVE).none(EntityTag.CLIMBING) },
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
        val downPressed = controller?.hasCommand(Command.MOVE_DOWN) == true

        updateHorizontalVelocity(velocity, physics, inputX, collision.isGrounded, physicsTimer.interval)
        applyMovement(collision, physics, velocity, downPressed)

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

    private fun applyMovement(collision: Collision, physics: Physics, velocity: Velocity, downPressed: Boolean) {
        moveAxis(collision, physics, velocity, velocity.current.x * physicsTimer.interval, isVertical = false)
        moveAxis(
            collision,
            physics,
            velocity,
            velocity.current.y * physicsTimer.interval,
            isVertical = true,
            downPressed
        )
    }

    private fun moveAxis(
        collision: Collision,
        physics: Physics,
        velocity: Velocity,
        delta: Float,
        isVertical: Boolean,
        downPressed: Boolean = false
    ) {
        if (delta == 0f) return

        if (isVertical) {
            val prevBottom = physics.position.y + collision.box.y
            physics.position.y += delta
            updateCheckRect(physics, collision)
            collision.isGrounded = false

            handleVerticalCollision(collision, velocity, physics, delta, prevBottom, downPressed)
        } else {
            physics.position.x += delta
            clampToMapBounds(physics, collision)
            updateCheckRect(physics, collision)

            if (checkTileCollision(includeSemiSolid = false)) {
                physics.position.x = if (delta > 0f) {
                    tileRect.x - collision.box.x - collision.box.width
                } else {
                    tileRect.x + tileRect.width - collision.box.x
                }
                velocity.current.x = 0f
            }
        }
    }

    private fun clampToMapBounds(physics: Physics, collision: Collision) {
        val minX = -collision.box.x
        val maxX = tiledService.mapWidth - collision.box.x - collision.box.width
        physics.position.x = physics.position.x.coerceIn(minX, maxX)
    }

    private fun handleVerticalCollision(
        collision: Collision,
        velocity: Velocity,
        physics: Physics,
        delta: Float,
        prevBottom: Float,
        downPressed: Boolean
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

        if (checkTileCollision(includeSemiSolid = true) && prevBottom >= tileRect.y + tileRect.height) {
            physics.position.y = tileRect.y + tileRect.height - collision.box.y
            velocity.current.y = 0f
            collision.isGrounded = true
            return
        }

        // Top ladder tile collision
        if (!downPressed) {
            checkTopLadderCollision(collision, physics, velocity, prevBottom)
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

    private fun checkTopLadderCollision(
        collision: Collision,
        physics: Physics,
        velocity: Velocity,
        prevBottom: Float
    ) {
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
                    physics.position.y = tileRect.y + tileRect.height - collision.box.y
                    velocity.current.y = 0f
                    collision.isGrounded = true
                    return
                }
            }
        }
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
