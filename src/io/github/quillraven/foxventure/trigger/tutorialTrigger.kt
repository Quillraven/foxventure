package io.github.quillraven.foxventure.trigger

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Animation.Companion.getGdxAnimation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Transform
import ktx.collections.gdxArrayOf
import ktx.math.vec2

fun IntervalSystem.tutorialCutscene() = trigger {

    action {
        onStart = {
            player().configure { it -= Controller }
        }
    }

    action {
        var acorn: Entity = Entity.NONE
        onStart = {
            val objectsAtlas = this.inject<TextureAtlas>()
            val acornAnimation = objectsAtlas.getGdxAnimation("objects/acorn", AnimationType.IDLE)
            val (playerPosition, playerSize) = player()[Transform]
            val halfAcornW = acornAnimation.keyFrames.first().regionWidth.toWorldUnits() / 2
            val acornX = playerPosition.x + playerSize.x / 2 - halfAcornW
            val acornY = playerPosition.y + playerSize.y / 2 + 10f

            acorn = entity {
                it += Transform(position = vec2(acornX, acornY), size = vec2(1f, 1f), z = 500)
                it += MoveTo(gdxArrayOf(MoveToPoint(vec2(acornX, acornY - 10f), Interpolation.linear, 2f)))
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
        var timer = 2.5f
        onStart = {
            audioService.playSound("hurt2.wav")
            player()[Animation].changeTo(AnimationType.DIZZY)
        }
        onUpdate = {
            timer -= deltaTime
            timer <= 0f
        }
    }

    action {
        onStart = {
            player()[Animation].changeTo(AnimationType.IDLE)
            // TODO: show textbox with fox losing memory dialogue, return true on ENTER/ESC
        }
    }

    action {
        onStart = {
            player().configure { it += Controller() }
        }
    }
}
