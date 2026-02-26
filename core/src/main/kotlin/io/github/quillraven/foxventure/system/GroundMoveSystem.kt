package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.system.RenderSystem.Companion.sfx
import io.github.quillraven.foxventure.tiled.TiledService
import ktx.math.vec2
import kotlin.math.abs
import kotlin.math.sign

class GroundMoveSystem(
    private val tiledService: TiledService = inject(),
    private val physicsTimer: PhysicsTimer = inject(),
    assets: AssetManager = inject(),
) : IteratingSystem(
    family = family { all(Velocity, Collision, Physics, EntityTag.ACTIVE).none(EntityTag.CLIMBING, EntityTag.ROOT) },
) {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]
    private val runDustAnimation: GdxAnimation

    init {
        val regions = objectsAtlas.findRegions("sfx/dust1/idle")
        runDustAnimation = GdxAnimation(1 / 17f, regions, PlayMode.NORMAL)
    }

    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val velocity = entity[Velocity]
        val collision = entity[Collision]
        val physics = entity[Physics]

        val inputX = getInputX(entity)

        val wasAtMaxSpeed = abs(velocity.current.x) >= physics.maxSpeed && collision.isGrounded

        updateHorizontalVelocity(entity, velocity, physics, inputX, collision.isGrounded, physicsTimer.interval)
        applyHorizontalMovement(velocity.current, physics.position, collision.box)

        val isAtMaxSpeed = abs(velocity.current.x) >= physics.maxSpeed && collision.isGrounded
        if (!wasAtMaxSpeed && isAtMaxSpeed && entity.has(Player)) {
            spawnRunDust(entity, 1.5f)
        }
    }

    private fun getInputX(entity: Entity): Float {
        val controller = entity.getOrNull(Controller)
        if (controller != null) {
            var input = 0f
            if (controller.hasCommand(Command.MOVE_LEFT)) input -= 1f
            if (controller.hasCommand(Command.MOVE_RIGHT)) input += 1f
            return input
        }

        return entity.getOrNull(Follow)?.moveDirection ?: 0f
    }

    private fun sCurveAcceleration(speedPercent: Float): Float {
        val t = speedPercent.coerceIn(0.25f, 1f)
        val smoothed = t * t * (3f - 2f * t)
        return 0.5f + smoothed * 0.5f
    }

    private fun updateHorizontalVelocity(
        entity: Entity,
        velocity: Velocity,
        physics: Physics,
        inputX: Float,
        isGrounded: Boolean,
        deltaTime: Float
    ) {
        val speed = velocity.current
        when {
            inputX != 0f -> {
                val acceleration = if (isGrounded) physics.acceleration else physics.acceleration * physics.airControl
                val wasSkidding = velocity.isSkidding
                velocity.isSkidding = speed.x != 0f && sign(inputX) != sign(speed.x)
                val rate = if (velocity.isSkidding) {
                    physics.skidDeceleration
                } else {
                    val speedPercent = abs(speed.x) / physics.maxSpeed
                    acceleration * sCurveAcceleration(speedPercent)
                }

                speed.x = when {
                    velocity.isSkidding -> adjustToZero(speed.x, rate * deltaTime)
                    else -> (speed.x + inputX * rate * deltaTime).coerceIn(-physics.maxSpeed, physics.maxSpeed)
                }

                if (!wasSkidding && velocity.isSkidding && entity has Player) {
                    spawnRunDust(entity, 0.9f)
                }
            }

            else -> {
                val deceleration = if (isGrounded) physics.deceleration else physics.deceleration * physics.airControl
                speed.x = adjustToZero(speed.x, deceleration * deltaTime)
                velocity.isSkidding = false
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

    private fun spawnRunDust(entity: Entity, size: Float) {
        val transform = entity[Transform]
        val collision = entity[Collision]
        val velocity = entity[Velocity].current

        val dustSize = vec2(size, size)
        val dustPosition = vec2(
            if (velocity.x > 0f) {
                // running right -> dust behind (left side)
                transform.position.x + collision.box.x - dustSize.x * 0.75f
            } else {
                // running left -> dust behind (right side)
                transform.position.x + collision.box.x + collision.box.width - dustSize.x * 0.75f
            },
            transform.position.y
        )

        world.sfx(dustPosition, dustSize, runDustAnimation, flip = velocity.x < 0f)
    }
}
