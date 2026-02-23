package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.tiled.GroundTile
import io.github.quillraven.foxventure.tiled.TiledService
import kotlin.math.abs

class ClimbSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, EntityTag.ACTIVE) },
) {
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
        if (!collision.isOnLadder) {
            // skip any other climb logic if not attached to a ladder yet
            val includeTileBelow = inputY < 0f
            val ladderTile = tiledService.getLadderTile(physics.position, collBox, includeTileBelow)
            attachToLadder(inputY, jumpPressed, ladderTile, physics, entity, collision)
            return
        }

        // At this point we know the entity is on a ladder
        //
        // check if there is no ladder anymore or the entity wants to abort climbing
        val ladderTile = tiledService.getLadderTile(physics.position, collBox, false)
        val velocity = entity[Velocity].current
        val abortClimbing = inputY == 0f && (getInputX(controller) != 0f || jumpPressed || ladderTile == null)
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
        tiledService.getCollisionRect(position, collision.box, false)?.let { groundTile ->
            position.y = groundTile.y + groundTile.height - collision.box.y
            stopClimbing(entity, collision, velocity)
            collision.isGrounded = true
        }
    }

    private fun attachToLadder(
        inputY: Float,
        jumpPressed: Boolean,
        ladderTile: GroundTile?,
        physics: Physics,
        entity: Entity,
        collision: Collision
    ) {
        if (inputY == 0f || jumpPressed || ladderTile == null) {
            // if no climb input or jump is pressed or no nearby ladder -> do nothing
            return
        }
        if (inputY < 0f && collision.isGrounded && !ladderTile.isLadderTop) {
            // climbing down only allowed on the top of a ladder tile. This prevents graphical jittering when pressing
            // DOWN while standing on the ground and the bottom of a ladder.
            return
        }

        // trying to climb -> attach to ladder if it is close enough
        val collBox = collision.box
        val ladderCenterX = ladderTile.rect.x + ladderTile.rect.width * 0.5f
        val playerCenterX = physics.position.x + collBox.x + collBox.width * 0.5f
        val tolerance = 0.3f

        if (abs(ladderCenterX - playerCenterX) <= tolerance) {
            startClimbing(entity, collision)
            physics.position.x = ladderTile.rect.x + ladderTile.rect.width * 0.5f - collBox.width * 0.5f - collBox.x
            physics.position.y = ladderTile.rect.y + ladderTile.rect.height * 0.5f - collBox.y
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
            jumpControl.isJumping = false
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

    private fun getLadderTop(
        position: Vector2,
        collisionBox: Rect,
    ): Rect? {
        val ladder = tiledService.getLadderTile(position, collisionBox, false) ?: return null
        if (ladder.isLadderTop) {
            return ladder.rect
        }
        return null
    }

    private fun checkLadderTop(
        entity: Entity,
        position: Vector2,
        collision: Collision,
        velocity: Vector2,
    ) {
        getLadderTop(position, collision.box)?.let { tileRect ->
            if (position.y >= tileRect.y + tileRect.height * 0.4f) {
                position.y = tileRect.y + tileRect.height - collision.box.y
                collision.isGrounded = true
                stopClimbing(entity, collision, velocity)
            }
        }
    }
}
