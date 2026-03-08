package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntity
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.Damage
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.system.DamagedSystem.Companion.damageEntity
import io.github.quillraven.foxventure.system.RenderSystem.Companion.sfx
import io.github.quillraven.foxventure.ui.GameViewModel

class CollisionSystem(
    private val physicsTimer: PhysicsTimer = inject(),
    private val audioService: AudioService = inject(),
    private val gameViewModel: GameViewModel = inject(),
    assets: AssetManager = inject(),
) : IteratingSystem(
    family = family { all(Transform, Collision, EntityTag.ACTIVE) },
    comparator = compareEntity { e1, e2 -> e1.id.compareTo(e2.id) }
) {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]
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
        val collBox = entity[Collision].box
        val type = entity.getOrNull(Type)?.type ?: ""

        family.forEach { other ->
            if (entity.id >= other.id) {
                // we sort the family by the entity id.
                // This check prevents checking the same entities multiple times within a single forEach loop.
                return@forEach
            }

            val otherTransform = other[Transform]
            val (otherPosition) = otherTransform
            val otherCollBox = other[Collision].box
            val otherType = other.getOrNull(Type)?.type ?: ""

            if (collBox.overlaps(position, otherPosition, otherCollBox)) {
                when {
                    "player" == type -> onPlayerCollision(
                        player = entity,
                        entityTransform,
                        collBox,
                        other = other,
                        otherTransform,
                        otherCollBox,
                        otherType = otherType
                    )

                    "player" == otherType -> onPlayerCollision(
                        player = other,
                        otherTransform,
                        otherCollBox,
                        other = entity,
                        entityTransform,
                        collBox,
                        otherType = type
                    )

                    else -> onCollision(entity = entity, other = other)
                }
            }
        }
    }

    @Suppress("unused")
    private fun onCollision(entity: Entity, other: Entity) = Unit

    private fun onPlayerCollision(
        player: Entity,
        playerTransform: Transform,
        playerCollBox: Rect,
        other: Entity,
        otherTransform: Transform,
        otherCollBox: Rect,
        otherType: String
    ) {
        when (otherType) {
            "gem" -> onPlayerGemCollision(player, other)
            "cherry" -> onPlayerCherryCollision(player, other)
            "damage" -> onPlayerDamageCollision(player, other)
            "spike" -> onPlayerSpikeCollision(player, other)
            "enemy" -> onPlayerEnemyCollision(player, other, playerTransform, playerCollBox, otherTransform, otherCollBox)
        }
    }

    private fun onPlayerSpikeCollision(
        player: Entity,
        other: Entity
    ) {
        world.damageEntity(
            source = other,
            target = player,
            damage = 1,
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
        playerCollBox: Rect,
        otherTransform: Transform,
        otherCollBox: Rect,
    ) {
        val playerBottom = playerTransform.position.y + playerCollBox.y
        val enemyTop = otherTransform.position.y + otherCollBox.y + otherCollBox.height * 0.75f

        if (playerBottom >= enemyTop) {
            // player stomps on an enemy from above -> apply upwards impulse
            val jumpPressed = player[Controller].hasCommand(Command.JUMP)
            val physics = player[Physics]
            player[Velocity].current.y = if (jumpPressed) physics.jumpImpulse * 0.75f else physics.jumpImpulse * 0.4f

            // damage enemy
            world.damageEntity(
                source = player,
                target = other,
                damage = 1,
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
            damage = 1,
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

    private fun spawnPickupSfx(transform: Transform, scale: Float) {
        val scaledSize = transform.size.cpy().scl(scale)
        val offset = (scaledSize.x - transform.size.x) * 0.5f
        val centeredPosition = transform.position.cpy().sub(offset, offset)

        world.sfx(centeredPosition, scaledSize, pickupAnimation, speed = 1.5f)
    }
}
