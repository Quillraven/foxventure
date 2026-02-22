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

class ClimbSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, EntityTag.ACTIVE) },
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
        val collBox = collision.box
        val physics = entity[Physics]
        val controller = entity.getOrNull(Controller)
        val inputY = getInputY(controller)
        val jumpPressed = controller?.hasCommand(Command.JUMP) == true

        // first check if we should start climbing
        val ladderRect = getNearbyLadder(physics.position, collBox)
        if (!collision.isOnLadder) {
            // skip any other climb logic if not attached to a ladder yet
            attachToLadder(inputY, jumpPressed, ladderRect, physics, collBox, entity, collision)
            return
        }

        // At this point we know the entity is on a ladder
        //
        // check if there is no ladder anymore or the entity wants to abort climbing
        val velocity = entity[Velocity].current
        val abortClimbing = getInputX(controller) != 0f || jumpPressed || ladderRect == null
        if (abortClimbing) {
            stopClimbing(entity, collision, velocity)
            return
        }

        // climb
        velocity.set(0f, inputY * physics.climbSpeed)
        val delta = velocity.y * physicsTimer.interval
        if (delta != 0f) {
            physics.position.y += delta
            if (delta < 0f) {
                checkLadderBottom(entity, physics.position, velocity, collision)
            } else {
                checkLadderTop(entity, physics.position, collision, velocity)
            }
        }
    }

    private fun checkLadderBottom(
        entity: Entity,
        position: Vector2,
        velocity: Vector2,
        collision: Collision
    ) {
        getGroundTile(position, collision.box)?.let { groundTile ->
            position.y = groundTile.y + groundTile.height - collision.box.y
            stopClimbing(entity, collision, velocity)
            collision.isGrounded = true
        }
    }

    private fun attachToLadder(
        inputY: Float,
        jumpPressed: Boolean,
        ladderRect: Rectangle?,
        physics: Physics,
        collBox: Box,
        entity: Entity,
        collision: Collision
    ) {
        if (inputY == 0f || jumpPressed || ladderRect == null) {
            // if no climb input or jump is pressed or no nearby ladder -> do nothing
            return
        }

        // trying to climb -> attach to ladder if it is close enough
        val ladderCenterX = ladderRect.x + ladderRect.width * 0.5f
        val playerCenterX = physics.position.x + collBox.x + collBox.width * 0.5f
        val tolerance = 0.3f

        if (abs(ladderCenterX - playerCenterX) <= tolerance) {
            startClimbing(entity, collision)
            physics.position.x = ladderRect.x + ladderRect.width * 0.5f - collBox.width * 0.5f - collBox.x
        }
    }

    private fun startClimbing(
        entity: Entity,
        collision: Collision
    ) {
        collision.isOnLadder = true
        collision.isGrounded = false
        entity.configure { it += EntityTag.CLIMBING }
    }

    private fun stopClimbing(
        entity: Entity,
        collision: Collision,
        velocity: Vector2
    ) {
        collision.isOnLadder = false
        velocity.y = 0f
        entity.configure { it -= EntityTag.CLIMBING }

        // reset jump settings to not accidentally start a midair jump immediately after stopping the climb
        entity[JumpControl].let { jumpControl ->
            jumpControl.jumpInput = false
            jumpControl.coyoteTimer = 0f
            jumpControl.jumpBufferTimer = 0f
        }
    }

    private fun getInputX(controller: Controller?): Float {
        if (controller == null) return 0f
        var input = 0f
        if (controller.hasCommand(Command.MOVE_LEFT)) input -= 1f
        if (controller.hasCommand(Command.MOVE_RIGHT)) input += 1f
        return input
    }

    private fun getInputY(controller: Controller?): Float {
        if (controller == null) return 0f
        var input = 0f
        if (controller.hasCommand(Command.MOVE_UP)) input += 1f
        if (controller.hasCommand(Command.MOVE_DOWN)) input -= 1f
        return input
    }

    private fun findCollidingTile(
        position: Vector2,
        collisionBox: Box,
        action: (cellX: Int, cellY: Int) -> Rectangle?
    ): Rectangle? {
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
                val result = action(x, y)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun getNearbyLadder(position: Vector2, collisionBox: Box): Rectangle? {
        return findCollidingTile(position, collisionBox) { cellX, cellY ->
            tiledService.getLadderRect(cellX, cellY, tileRect)
            if (tileRect.width > 0f && checkRect.overlaps(tileRect)) {
                return@findCollidingTile tileRect
            }
            return@findCollidingTile null
        }
    }

    private fun getGroundTile(
        position: Vector2,
        collisionBox: Box
    ): Rectangle? {
        return findCollidingTile(position, collisionBox) { cellX, cellY ->
            tiledService.getCollisionRect(cellX, cellY, true, tileRect)
            if (tileRect.width > 0f && checkRect.overlaps(tileRect)) {
                return@findCollidingTile tileRect
            }
            return@findCollidingTile null
        }
    }

    private fun getTopLadder(
        position: Vector2,
        collisionBox: Box,
    ): Rectangle? {
        return findCollidingTile(position, collisionBox) { cellX, cellY ->
            tiledService.getLadderRect(cellX, cellY, tileRect)
            if (tileRect.width > 0f && checkRect.overlaps(tileRect) && tiledService.isTopLadderTile(cellX, cellY)) {
                return@findCollidingTile tileRect
            }
            return@findCollidingTile null
        }
    }

    private fun checkLadderTop(
        entity: Entity,
        position: Vector2,
        collision: Collision,
        velocity: Vector2,
    ) {
        getTopLadder(position, collision.box)?.let { tileRect ->
            if (position.y >= tileRect.y + tileRect.height * 0.4f) {
                position.y = tileRect.y + tileRect.height - collision.box.y
                collision.isGrounded = true
                stopClimbing(entity, collision, velocity)
            }
        }
    }
}
