package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Flash
import io.github.quillraven.foxventure.component.Graphic
import kotlin.math.ceil

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
            val numFlashes = 5f * ceil(flash.duration).toInt()
            val progress = (flash.timer / flash.duration) * numFlashes
            entity[Graphic].color.a = Interpolation.sine.apply(0.3f, 1f, (progress % 1f))
        }
    }
}
