package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity

class InterpolationSystem(
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Transform, Velocity) }
) {
    override fun onTickEntity(entity: Entity) {
        val (_, prevPosition, targetPosition) = entity[Velocity]
        entity[Transform].position.set(
            MathUtils.lerp(prevPosition.x, targetPosition.x, physicsTimer.alpha),
            MathUtils.lerp(prevPosition.y, targetPosition.y, physicsTimer.alpha),
        )
    }
}