package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.AttackRange
import io.github.quillraven.foxventure.component.EntityTag

class AttackRangeSystem : IteratingSystem(
    family = family { all(AttackRange, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val attackRange = entity[AttackRange]
        if (attackRange.time > 0f) {
            attackRange.time -= deltaTime
        }
    }
}
