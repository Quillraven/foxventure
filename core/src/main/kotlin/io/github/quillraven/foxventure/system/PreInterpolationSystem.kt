package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Transform

class PreInterpolationSystem : IteratingSystem(
    family = family { all(Transform, Physics, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val physics = entity[Physics]
        physics.prevPosition.set(physics.position)
    }
}