package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Flash
import io.github.quillraven.foxventure.component.Graphic

class FlashSystem : IteratingSystem(
    family = family { all(Flash, Graphic, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val flash = entity[Flash]
        flash.timer += deltaTime

        if (flash.timer >= flash.duration) {
            entity[Graphic].color.a = 1f
            entity.configure { it -= Flash }
        } else {
            val progress = (flash.timer * 5f) % 1f // 5 flashes per second
            entity[Graphic].color.a = Interpolation.pow2Out.apply(1f, 0.3f, progress)
        }
    }
}
