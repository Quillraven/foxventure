package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntity
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Tiled
import io.github.quillraven.foxventure.component.Transform

class CollisionSystem(
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Transform, Collision, EntityTag.ACTIVE) },
    comparator = compareEntity { e1, e2 -> e1.id.compareTo(e2.id) }
) {
    override fun onTick() {
        repeat(physicsTimer.numSteps) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val (position) = entity[Transform]
        val collBox = entity[Collision].box
        val type = entity.getOrNull(Tiled)?.type ?: ""

        family.forEach { other ->
            if (entity.id >= other.id) {
                // we sort the family by the entity id.
                // This check prevents checking the same entities multiple times within a single forEach loop.
                return@forEach
            }

            val (otherPosition) = other[Transform]
            val otherCollBox = other[Collision].box
            val otherType = other.getOrNull(Tiled)?.type ?: ""

            if (collBox.overlaps(position, otherPosition, otherCollBox)) {
                when {
                    "player" == type -> onPlayerCollision(player = entity, other = other, otherType = otherType)
                    "player" == otherType -> onPlayerCollision(player = other, other = entity, otherType = type)
                    else -> onCollision(entity = entity, other = other)
                }
            }
        }
    }

    private fun onCollision(entity: Entity, other: Entity) {
        // for now not needed
    }

    private fun onPlayerCollision(player: Entity, other: Entity, otherType: String) {
        when (otherType) {
            "gem" -> {
                player[Player].gems++
                other.remove()
            }
        }
    }
}
