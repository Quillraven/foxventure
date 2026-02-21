package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic

class AnimationSystem : IteratingSystem(
    family = family { all(Animation, Graphic, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val animation = entity[Animation]
        entity[Graphic].region = animation.active.getKeyFrame(animation.stateTime)
        animation.stateTime += deltaTime
    }
}