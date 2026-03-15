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
import io.github.quillraven.foxventure.component.DelayRemoval
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transform.Companion.Z_SFX
import ktx.collections.gdxArrayOf
import ktx.math.vec2

fun IntervalSystem.tutorialCutscene() = trigger {

    action {
        onStart = {
            player().configure {
                it -= Controller
                it -= JumpControl
            }
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
                it += Transform(position = vec2(acornX, acornY), size = vec2(1f, 1f), z = Z_SFX)
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

    timedAction(2.5f) {
        onStart = {
            audioService.playSound("hurt2.wav")
            player()[Animation].changeTo(AnimationType.DIZZY)

            val objectsAtlas = this.inject<TextureAtlas>()
            val stunAnimation = objectsAtlas.getGdxAnimation("sfx/stun", AnimationType.IDLE)
            val stunFrame = stunAnimation.keyFrames.first()
            val (playerPosition) = player()[Transform]
            val size = vec2(stunFrame.regionWidth.toWorldUnits(), stunFrame.regionHeight.toWorldUnits())
            entity {
                it += Transform(position = playerPosition.cpy().add(0.3f, 0.9f), size = size, z = Z_SFX)
                it += Graphic(stunFrame)
                it += Animation("objects/acorn", stunAnimation, emptyMap(), 1f)
                it += DelayRemoval(2.5f)
                it += EntityTag.ACTIVE
            }
        }
    }

    timedAction(9f) {
        onStart = {
            player()[Animation].changeTo(AnimationType.IDLE)
            gameViewModel.onShowMessage(
                "avatar-fox",
                "{SHAKE}Ouch{WAIT=1.0}{RESET} - that hurt!\n" +
                        "Darn it ... I think I lost my memory. I can only remember how to move."
            )
        }
    }

    timedAction(6f) {
        onStart = {
            gameViewModel.onShowMessage(
                "",
                "Move the fox by pressing A/D or LEFT/RIGHT arrow keys."
            )
        }
    }

    action {
        onStart = {
            gameViewModel.onHideMessage()
            player().configure { it += Controller() }
        }
    }
}
