package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.PhysicsConfig
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs

class ClimbSystem(
    private val tiledService: TiledService = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Transform, Collision, PhysicsConfig, JumpControl, EntityTag.ACTIVE) },
    interval = Fixed(1 / 60f),
) {
    private val tileRect = Rectangle()
    private val checkRect = Rectangle()

    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity]
        val collision = entity[Collision]
        val physics = entity[PhysicsConfig]
        val jumpControl = entity[JumpControl]

        velocity.prevPosition.set(velocity.targetPosition)

        val controller = entity.getOrNull(Controller)
        val inputX = getInputX(controller)
        val inputY = getInputY(controller)
        val jumpPressed = controller?.hasCommand(Command.JUMP) == true

        val ladderNearby = checkLadderNearby(velocity, collision)

        // Attach to ladder
        if (ladderNearby && inputY != 0f && !jumpPressed && !collision.isOnLadder) {
            val ladderCenterX = tileRect.x + tileRect.width * 0.5f
            val playerCenterX = velocity.targetPosition.x + collision.box.x + collision.box.width * 0.5f

            if (abs(ladderCenterX - playerCenterX) <= 0.3f) {
                collision.isOnLadder = true
                velocity.targetPosition.x =
                    tileRect.x + tileRect.width * 0.5f - collision.box.width * 0.5f - collision.box.x
                entity.configure { it += EntityTag.CLIMBING }
            }
        }

        // Exit ladder
        if (collision.isOnLadder && (inputX != 0f || jumpPressed || !ladderNearby)) {
            collision.isOnLadder = false
            jumpControl.jumpInput = false
            jumpControl.coyoteTimer = 0f
            jumpControl.jumpBufferTimer = 0f
            entity.configure { it -= EntityTag.CLIMBING }
        }

        // Climb
        if (collision.isOnLadder) {
            collision.isGrounded = false
            jumpControl.coyoteTimer = 0f
            jumpControl.jumpBufferTimer = 0f
            velocity.current.set(0f, inputY * physics.climbSpeed)
            
            val delta = velocity.current.y * deltaTime
            if (delta != 0f) {
                velocity.targetPosition.y += delta
                updateCheckRect(velocity, collision)
                
                if (delta < 0f && checkTileCollision()) {
                    velocity.targetPosition.y = tileRect.y + tileRect.height - collision.box.y
                    velocity.current.y = 0f
                    collision.isGrounded = true
                }
            }
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

    private fun updateCheckRect(velocity: Velocity, collision: Collision) {
        checkRect.set(
            velocity.targetPosition.x + collision.box.x,
            velocity.targetPosition.y + collision.box.y,
            collision.box.width,
            collision.box.height
        )
    }

    private fun checkLadderNearby(velocity: Velocity, collision: Collision): Boolean {
        updateCheckRect(velocity, collision)

        val startX = checkRect.x.toInt()
        val endX = (checkRect.x + checkRect.width).toInt()
        val startY = checkRect.y.toInt()
        val endY = (checkRect.y + checkRect.height).toInt()

        for (y in startY..endY) {
            for (x in startX..endX) {
                tiledService.getLadderRect(x, y, tileRect)
                if (tileRect.width > 0f && checkRect.overlaps(tileRect)) return true
            }
        }
        return false
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

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        if (entity.has(EntityTag.CLIMBING)) {
            val (_, prevPosition, targetPosition) = entity[Velocity]
            entity[Transform].position.set(
                com.badlogic.gdx.math.MathUtils.lerp(prevPosition.x, targetPosition.x, alpha),
                com.badlogic.gdx.math.MathUtils.lerp(prevPosition.y, targetPosition.y, alpha),
            )
        }
    }
}
