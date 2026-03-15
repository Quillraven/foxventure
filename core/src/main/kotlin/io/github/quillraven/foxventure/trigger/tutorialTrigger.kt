package io.github.quillraven.foxventure.trigger

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Animation.Companion.getGdxAnimation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import ktx.collections.gdxArrayOf
import ktx.math.vec2

fun tutorialCutscene() = trigger {

    action {
        onStart = {
            val player = family { all(Player) }.single()
            player.configure { it -= Controller }
        }
    }

    action {
        var acorn: Entity = Entity.NONE
        onStart = {
            val objectsAtlas = this.inject<TextureAtlas>()
            val acornAnimation = objectsAtlas.getGdxAnimation("objects/acorn", AnimationType.IDLE)

            val playerPos = family { all(Player) }.single()[Transform].position
            acorn = entity {
                it += Transform(position = vec2(playerPos.x, playerPos.y + 10f), size = vec2(1f, 1f), z = 500)
                it += MoveTo(gdxArrayOf(MoveToPoint(vec2(playerPos.x, playerPos.y), Interpolation.linear, 2f)))
                it += Graphic(acornAnimation.keyFrames.first())
                it += Animation("objects/acorn", acornAnimation, emptyMap(), 1f)
                it += EntityTag.ACTIVE
                it += EntityTag.PROJECTILE
            }
        }
        onUpdate = {
            val acornOnHead = acorn hasNo MoveTo
            if (acornOnHead) {
                acorn.remove()
            }
            acornOnHead
        }
    }

    action {
        var timer = 2f
        onStart = {
            family { all(Player) }.single()[Animation].changeTo(AnimationType.DIZZY)
        }
        onUpdate = {
            timer -= deltaTime
            timer <= 0f
        }
    }

    action {
        onStart = {
            family { all(Player) }.single()[Animation].changeTo(AnimationType.IDLE)
            // TODO: show textbox with fox losing memory dialogue, set action.done = true on ENTER/ESC
        }
    }

    action {
        onStart = {
            val player = family { all(Player) }.single()
            player.configure { it += Controller() }
        }
    }
}
