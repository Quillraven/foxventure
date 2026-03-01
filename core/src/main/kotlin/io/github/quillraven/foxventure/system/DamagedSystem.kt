package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Flash
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Velocity

class DamagedSystem(
    private val audioService: AudioService = inject(),
) : IteratingSystem(
    family = family { all(Damaged, Life) }
) {
    override fun onTickEntity(entity: Entity) {
        val damaged = entity[Damaged]
        if (damaged.timer >= damaged.invulnerableTime) {
            entity.configure { it -= Damaged }
            return
        }

        if (damaged.timer == 0f) {
            damaged.timer = 0.001f
            val life = entity[Life]
            life.amount -= damaged.damage

            // flash if still alive
            if (life.amount > 0) {
                entity.configure { it += Flash(damaged.invulnerableTime) }
            }

            // detach from ladder
            if (entity has EntityTag.CLIMBING) {
                entity.configure { it -= EntityTag.CLIMBING }
            }

            // push entity back slightly
            val direction = if (damaged.source.wasRemoved()) {
                if (entity[Graphic].flip) 1f else -1f
            } else {
                if (damaged.source[Graphic].flip) -1f else 1f
            }
            entity.getOrNull(Velocity)?.let { velocity ->
                velocity.current.x = 6f * direction
            }

            // play sound
            audioService.playSound(damaged.soundName)

            return
        }

        damaged.timer += deltaTime
    }

    companion object {
        fun World.damageEntity(
            source: Entity,
            target: Entity,
            damage: Int,
            invulnerableTime: Float,
            soundName: String,
        ): Boolean {
            if (target.has(Damaged)) return false // target invulnerable -> ignore damage

            target.configure { it += Damaged(source, invulnerableTime = invulnerableTime, damage, soundName) }
            return true
        }
    }
}