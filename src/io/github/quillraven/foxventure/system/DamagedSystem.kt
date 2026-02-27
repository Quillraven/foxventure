package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Velocity

class DamagedSystem : IteratingSystem(
    family = family { all(Damaged, Life) }
) {
    override fun onTickEntity(entity: Entity) {
        val damaged = entity[Damaged]
        if (damaged.timer >= damaged.invulnerableTime) {
            entity.configure { it -= Damaged }
            return
        }

        if (damaged.timer == 0f) {
            damaged.timer = 0.001f
            entity[Life].amount -= damaged.damage

            // detach from ladder
            if (entity has EntityTag.CLIMBING) {
                entity.configure { it -= EntityTag.CLIMBING }
            }

            // push entity back slightly
            val direction = if (damaged.source.wasRemoved()) {
                if (entity[Graphic].flip) 1f else -1f
            } else {
                if (damaged.source[Graphic].flip) -1f else 1f
            }
            entity.getOrNull(Velocity)?.let { velocity ->
                velocity.current.x = 6f * direction
            }

            return
        }

        damaged.timer += deltaTime
    }
}