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
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.input.Command
import io.github.quillraven.foxventure.system.RenderSystem.Companion.sfx

class CollisionSystem(
    private val physicsTimer: PhysicsTimer = inject(),
    private val audioService: AudioService = inject(),
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
            "gem" -> {
                player[Player].gems++

                val transform = other[Transform]
                spawnPickupSfx(transform)

                other.remove()
                audioService.playSound("pickup.wav")
            }

            "damage" if (player hasNo Damaged) -> {
                val (source, damageAmount) = other[Damage]
                player.configure { it += Damaged(source, invulnerableTime = 1f, damageAmount) }
                other.remove()
            }

            "enemy" -> {
                val playerBottom = playerTransform.position.y + playerCollBox.y
                val enemyTop = otherTransform.position.y + otherCollBox.y + otherCollBox.height
                val tolerance = 0.25f
                val playerVelocity = player[Velocity].current

                if (playerBottom + tolerance > enemyTop && playerVelocity.y < 0f && other hasNo Damaged) {
                    // player stomps on an enemy from above
                    val jumpPressed = player[Controller].hasCommand(Command.JUMP)
                    val physics = player[Physics]
                    playerVelocity.y = if (jumpPressed) physics.jumpImpulse else physics.jumpImpulse * 0.7f

                    other.configure { it += Damaged(player, invulnerableTime = 0.5f, damage = 1) }
                } else if (player hasNo Damaged) {
                    // the player collides from the side or below - the player gets damaged
                    player.configure { it += Damaged(other, invulnerableTime = 1f, damage = 1) }
                }
            }
        }
    }

    private fun spawnPickupSfx(transform: Transform) {
        val scaledSize = transform.size.cpy().scl(1.5f)
        val offset = (scaledSize.x - transform.size.x) * 0.5f
        val centeredPosition = transform.position.cpy().sub(offset, offset)

        world.sfx(centeredPosition, scaledSize, pickupAnimation, speed = 1.5f)
    }
}
