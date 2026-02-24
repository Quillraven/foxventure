package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm

class FsmSystem : IteratingSystem(
    family = family { all(Fsm, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[Fsm].state.update()
    }
}