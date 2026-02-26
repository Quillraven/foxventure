package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.DamageRequest
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import ktx.math.vec2

data object MushroomStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.IDLE)
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity]

        if (entity[Attack].readyToAttack) {
            entity[Fsm].state.changeState(MushroomStateAttack)
        } else if (velocity.current.x != 0f) {
            entity[Fsm].state.changeState(MushroomStateRun)
        }
    }
}

data object MushroomStateRun : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].changeTo(AnimationType.RUN)
    }

    override fun World.onUpdate(entity: Entity) {
        val velocity = entity[Velocity]

        if (entity[Attack].readyToAttack) {
            entity[Fsm].state.changeState(MushroomStateAttack)
        } else if (velocity.current.x == 0f) {
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }
}

data object MushroomStateAttack : FsmState {
    private var idleWidth = 0f
    private var idleHeight = 0f
    private var attackWidth = 0f
    private var attackHeight = 0f
    private var widthDiff = 0f

    private fun initAnimationDimensions(animation: Animation) {
        if (idleWidth != 0f) return

        idleWidth = animation.idle.getKeyFrame(0f).regionWidth.toWorldUnits()
        idleHeight = animation.idle.getKeyFrame(0f).regionHeight.toWorldUnits()
        val attackAnim = animation.get(AnimationType.ATTACK)
        attackWidth = attackAnim.getKeyFrame(0f).regionWidth.toWorldUnits()
        attackHeight = attackAnim.getKeyFrame(0f).regionHeight.toWorldUnits()
        widthDiff = attackWidth - idleWidth
    }

    override fun World.onEnter(entity: Entity) {
        val animation = entity[Animation]
        initAnimationDimensions(animation)
        animation.changeTo(AnimationType.ATTACK)

        // adjust Transform/Graphic because the attack frame has different dimensions than the idle/run frames
        val transform = entity[Transform]
        transform.size.set(attackWidth, attackHeight)
        val graphic = entity[Graphic]
        graphic.offset.x = if (graphic.flip) -widthDiff else 0f

        // stop the entity from moving
        entity.configure { it += EntityTag.ROOT }

        // spawn damage entity
        entity {
            val collBox = entity[Collision].box
            val damageX = transform.position.x + collBox.x + (collBox.width * 0.5f)
            val damageY = transform.position.y + collBox.y
            val damageOffsetX = if (graphic.flip) -widthDiff - collBox.x else 0f
            val damagePosition = vec2(damageX + damageOffsetX, damageY)
            val damageSize = vec2(1.9f, 1.5f)
            it += DamageRequest(entity[Attack].damage, damagePosition, damageSize, lifeSpan = 2f)
        }
    }

    override fun World.onUpdate(entity: Entity) {
        if (entity[Animation].isFinished()) {
            entity[Attack].resetCooldown()
            entity[Fsm].state.changeState(MushroomStateIdle)
        }
    }

    override fun World.onExit(entity: Entity) {
        val transform = entity[Transform]
        transform.size.set(idleWidth, idleHeight)
        entity[Graphic].offset.x = 0f
        entity.configure { it -= EntityTag.ROOT }
    }
}