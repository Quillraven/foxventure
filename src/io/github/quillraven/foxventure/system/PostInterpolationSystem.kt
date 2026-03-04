package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Transform
import ktx.math.component1
import ktx.math.component2

class PostInterpolationSystem(
    private val physicsTimer: PhysicsTimer = inject(),
) : IteratingSystem(
    family = family { all(Transform, Physics, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val physics = entity[Physics]
        val (prevX, prevY) = physics.prevPosition
        val (targetX, targetY) = physics.position
        entity[Transform].position.set(
            MathUtils.lerp(prevX, targetX, physicsTimer.alpha),
            MathUtils.lerp(prevY, targetY, physicsTimer.alpha),
        )
    }
}