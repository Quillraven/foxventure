package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.DelayRemoval

class DelayRemovalSystem : IteratingSystem(
    family = family { all(DelayRemoval) }
) {
    override fun onTickEntity(entity: Entity) {
        val delayRemoval = entity[DelayRemoval]
        delayRemoval.timer -= deltaTime

        if (delayRemoval.timer <= 0f) {
            entity.remove()
        }
    }
}
