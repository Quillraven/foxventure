package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntity
import io.github.quillraven.foxventure.AudioService
import io.github.quillraven.foxventure.PhysicsTimer
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.Damage
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Flash
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Invulnerable
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.component.Victory
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.system.DamagedSystem.Companion.damageEntity
import io.github.quillraven.foxventure.system.RenderSystem.Companion.sfx
import io.github.quillraven.foxventure.ui.GameViewModel
import ktx.app.gdxError
import ktx.math.vec2

/**
 * Detects collisions between entities and handles damage, pickups, and collision responses.
 */
class CollisionSystem(
    private val physicsTimer: PhysicsTimer = inject(),
    private val audioService: AudioService = inject(),
    private val gameViewModel: GameViewModel = inject(),
    objectsAtlas: TextureAtlas = inject(),
) : IteratingSystem(
    family = family { all(Transform, Collision, EntityTag.ACTIVE) },
    comparator = compareEntity { e1, e2 -> e1.id.compareTo(e2.id) }
) {
    private val pickupAnimation: GdxAnimation

    init {
        val regions = objectsAtlas.findRegions("sfx/item-feedback/idle")
        pickupAnimation = GdxAnimation(1 / 12f, regions, PlayMode.NORMAL)
    }

    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val entityTransform = entity[Transform]
        val (position) = entityTransform
        val collision = entity[Collision]
        val collBox = collision.box
        val type = entity.getOrNull(Type)?.type ?: ""

        family.forEach { other ->
            if (entity.id >= other.id) {
                // we sort the family by the entity id.
                // This check prevents checking the same entities multiple times within a single forEach loop.
                return@forEach
            }

            val otherTransform = other[Transform]
            val (otherPosition) = otherTransform
            val otherCollision = other[Collision]
            val otherCollBox = otherCollision.box
            val otherType = other.getOrNull(Type)?.type ?: ""

            if (collBox.overlaps(position, otherPosition, otherCollBox)) {
                when {
                    "player" == type -> onPlayerCollision(
                        player = entity,
                        entityTransform,
                        collision,
                        other = other,
                        otherTransform,
                        otherCollision,
                        otherType = otherType
                    )

                    "player" == otherType -> onPlayerCollision(
                        player = other,
                        otherTransform,
                        otherCollision,
                        other = entity,
                        entityTransform,
                        collision,
                        otherType = type
                    )
                }
            }
        }
    }

    private fun onPlayerCollision(
        player: Entity,
        playerTransform: Transform,
        playerCollision: Collision,
        other: Entity,
        otherTransform: Transform,
        otherCollision: Collision,
        otherType: String
    ) {
        when (otherType) {
            "gem" -> onPlayerGemCollision(player, other)
            "cherry" -> onPlayerCherryCollision(player, other)
            "gold-cherry" -> onPlayerGoldCherryCollision(player, other)
            "damage" -> onPlayerDamageCollision(player, other)
            "spike" -> onPlayerSpikeCollision(player, other, otherCollision)
            "enemy" -> onPlayerEnemyCollision(player, other, playerTransform, playerCollision, otherTransform, otherCollision)
            "house" -> onPlayerHouseCollision(player, other, playerTransform, otherTransform, otherCollision)
            else -> gdxError("Unsupported player collision with entity $otherType")
        }
    }

    private fun onPlayerSpikeCollision(
        player: Entity,
        other: Entity,
        otherCollision: Collision
    ) {
        world.damageEntity(
            source = other,
            target = player,
            damage = otherCollision.collisionDamage,
            invulnerableTime = 2.5f,
            stunDuration = 0.25f,
            soundName = "hurt2.wav",
            pushBackForce = 8f,
        )
    }

    private fun onPlayerEnemyCollision(
        player: Entity,
        other: Entity,
        playerTransform: Transform,
        playerCollision: Collision,
        otherTransform: Transform,
        otherCollision: Collision,
    ) {
        val playerBottom = playerTransform.position.y + playerCollision.box.y
        val enemyTop = otherTransform.position.y + otherCollision.box.y + otherCollision.box.height * 0.75f

        if (playerBottom >= enemyTop) {
            // player stomps on an enemy from above -> apply upwards impulse
            val jumpPressed = player[Controller].hasCommand(Command.JUMP)
            val physics = player[Physics]
            player[Velocity].current.y = if (jumpPressed) physics.jumpImpulse * 0.75f else physics.jumpImpulse * 0.4f

            // damage enemy
            world.damageEntity(
                source = player,
                target = other,
                damage = playerCollision.collisionDamage,
                invulnerableTime = 0.5f,
                stunDuration = 0.25f,
                soundName = "hurt1.wav",
                pushBackForce = 0f,
            )
            return
        }

        // the player collides from the side or below - the player gets damaged
        world.damageEntity(
            source = other,
            target = player,
            damage = otherCollision.collisionDamage,
            invulnerableTime = 2f,
            stunDuration = 0.5f,
            soundName = "hurt2.wav",
            pushBackForce = 7f,
        )
    }

    private fun onPlayerDamageCollision(player: Entity, other: Entity) {
        val (source, damageAmount) = other[Damage]
        if (world.damageEntity(
                source,
                target = player,
                damageAmount,
                invulnerableTime = 2f,
                stunDuration = 0.5f,
                soundName = "hurt2.wav",
                pushBackForce = 6f
            )
        ) {
            other.remove()
        }
    }

    private fun onPlayerGemCollision(player: Entity, other: Entity) {
        val playerComponent = player[Player]
        playerComponent.gems++
        gameViewModel.gems = playerComponent.gems

        val transform = other[Transform]
        spawnPickupSfx(transform, scale = 1.5f)

        other.remove()
        audioService.playSound("pickup.wav")
    }

    private fun onPlayerCherryCollision(
        player: Entity,
        other: Entity
    ) {
        player[Life].heal = 4

        val transform = other[Transform]
        spawnPickupSfx(transform, scale = 1f)

        other.remove()
        audioService.playSound("heal.wav")
    }

    private fun onPlayerGoldCherryCollision(
        player: Entity,
        other: Entity
    ) {
        // make player invulnerable
        player.configure {
            val invulnerable = it.getOrAdd(Invulnerable) { Invulnerable(0f) }
            val flash = it.getOrAdd(Flash) { Flash(0f) }
            invulnerable.duration = 10f
            flash.duration = 10f
        }

        // play invulnerability jingle
        audioService.playTempMusic("overdrive_loop.mp3", duration = 10f)

        // display pickup sfx and remove cherry
        val transform = other[Transform]
        spawnPickupSfx(transform, scale = 1f)

        other.remove()
    }

    private fun onPlayerHouseCollision(
        player: Entity,
        house: Entity,
        playerTransform: Transform,
        houseTransform: Transform,
        houseCollision: Collision,
    ) {
        val (otherPosition) = houseTransform
        val houseX = otherPosition.x + houseCollision.box.x + houseCollision.box.width / 2f - playerTransform.size.x
        val houseY = otherPosition.y + houseCollision.box.y
        player.configure { it += Victory(housePosition = vec2(houseX, houseY)) }
        house.configure { it -= Collision }
    }

    private fun spawnPickupSfx(transform: Transform, scale: Float) {
        val scaledSize = transform.size.cpy().scl(scale)
        val offset = (scaledSize.x - transform.size.x) * 0.5f
        val centeredPosition = transform.position.cpy().sub(offset, offset)

        world.sfx(centeredPosition, scaledSize, pickupAnimation, speed = 1.5f)
    }
}
