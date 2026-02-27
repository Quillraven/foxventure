package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damage
import io.github.quillraven.foxventure.component.DamageRequest
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type

class DamageRequestSystem : IteratingSystem(
    family = family { all(DamageRequest) }
) {
    override fun onTickEntity(entity: Entity) {
        val (source, damage, position, size, lifeSpan) = entity[DamageRequest]
        if (source.wasRemoved()) {
            entity.remove()
            return
        }

        val flip = source.getOrNull(Graphic)?.flip ?: false
        world.entity {
            val damageOffsetX = if (flip) -size.x else 0f
            it += Transform(position.add(damageOffsetX, 0f), size)
            it += Collision(box = Rect(0f, 0f, size.x, size.y))
            it += Damage(source = source, amount = damage)
            it += DelayRemoval(timer = lifeSpan)
            it += Type("damage")
            it += EntityTag.ACTIVE
        }
        entity.remove()
    }
}