package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Stun

class StunSystem : IteratingSystem(
    family = family { all(Stun) }
) {
    override fun onTickEntity(entity: Entity) {
        val stun = entity[Stun]

        stun.duration -= deltaTime
        if (stun.duration <= 0f) {
            entity.configure { it -= Stun }
        }
    }
}
