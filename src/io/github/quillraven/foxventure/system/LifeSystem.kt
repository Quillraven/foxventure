package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Life

class LifeSystem : IteratingSystem(
    family = family { all(Life, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        if (entity[Life].amount <= 0) {
            entity.remove()
        }
    }
}
