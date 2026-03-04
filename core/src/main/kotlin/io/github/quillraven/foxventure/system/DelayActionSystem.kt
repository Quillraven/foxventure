package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.DelayAction

class DelayActionSystem : IteratingSystem(
    family = family { all(DelayAction) }
) {
    override fun onTickEntity(entity: Entity) {
        val action = entity[DelayAction]
        action.delay -= deltaTime
        if (action.delay > 0f) return

        action.action()
        if (action.removeAfterAction) {
            entity.remove()
        } else {
            entity.configure { it -= DelayAction }
        }
    }
}