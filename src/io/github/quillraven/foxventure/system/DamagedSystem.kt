package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.CameraShake
import io.github.quillraven.foxventure.component.Damaged
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Flash
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Invulnerable
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Stun
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.ui.GameViewModel

class DamagedSystem(
    private val audioService: AudioService = inject(),
    private val gameViewModel: GameViewModel = inject(),
) : IteratingSystem(
    family = family { all(Damaged, Life) }
) {
    override fun onTickEntity(entity: Entity) {
        val damaged = entity[Damaged]
        onDamageTaken(entity, damaged)
        entity.configure { it -= Damaged }
    }

    private fun onDamageTaken(
        entity: Entity,
        damaged: Damaged,
    ) {
        val life = entity[Life]
        life.amount -= damaged.damage

        if (entity has Player) {
            // update UI if the player is damaged
            gameViewModel.life = life.amount
            // add a camera shake if the player is taking damage
            entity.configure { it += CameraShake(max = 4f, duration = 1.25f) }
        }

        // detach from ladder
        if (entity has EntityTag.CLIMBING) {
            entity.configure { it -= EntityTag.CLIMBING }
        }

        // push entity back
        val velocity = entity.getOrNull(Velocity)
        if (damaged.pushBackForce != 0f && velocity != null) {
            val sourceTransform = damaged.source.getOrNull(Transform)
            val direction = if (damaged.source.wasRemoved()) {
                if (entity[Graphic].flip) 1f else -1f
            } else if (sourceTransform != null) {
                val sourceCenterX = sourceTransform.position.x + sourceTransform.size.x / 2f
                val entityCenterX = entity[Transform].position.x + entity[Transform].size.x / 2f
                if (sourceCenterX > entityCenterX) -1f else 1f
            } else {
                if (damaged.source[Graphic].flip) -1f else 1f
            }
            velocity.current.x = damaged.pushBackForce * direction
        }

        // play sound
        audioService.playSound(damaged.soundName)
    }

    companion object {
        fun World.damageEntity(
            source: Entity,
            target: Entity,
            damage: Int,
            invulnerableTime: Float,
            stunDuration: Float,
            soundName: String,
            pushBackForce: Float,
        ): Boolean {
            if (target has Invulnerable) return false // target invulnerable -> ignore damage

            target.configure {
                it += Damaged(source, damage, soundName, pushBackForce)
                if (invulnerableTime > 0f) {
                    it += Invulnerable(invulnerableTime)
                }
                if (stunDuration > 0f) {
                    it += Stun(stunDuration)
                }
                // flash if still alive
                if (invulnerableTime > 0f && target[Life].amount - damage > 0) {
                    target += Flash(invulnerableTime)
                }
            }
            return true
        }
    }
}