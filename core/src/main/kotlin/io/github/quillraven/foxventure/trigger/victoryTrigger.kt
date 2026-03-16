package io.github.quillraven.foxventure.trigger

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.component.Victory
import io.github.quillraven.foxventure.system.TransitionType
import ktx.collections.gdxArrayOf
import ktx.math.vec2

fun IntervalSystem.victoryTrigger(victoryTextDuration: Float, victoryText: String) = trigger {
    timedAction(1f) {
        onStart = {
            audioService.fadeOutMusic(2f)
        }

        onUpdate = {
            MathUtils.isEqual(player()[Velocity].current.x, 0f, 0.01f)
        }
    }

    action {
        onStart = {
            player().run {
                val houseCenter = this[Victory].housePosition
                this.configure {
                    it -= Physics
                    it += MoveTo(gdxArrayOf(MoveToPoint(houseCenter, Interpolation.linear, 1f)))
                }
                this[Animation].changeTo(AnimationType.RUN)
            }
        }
        onUpdate = { player() hasNo MoveTo }
    }

    timedAction(2f) {
        onStart = {
            audioService.playMusic("8-bit-on-short.mp3")
            player()[Animation].changeTo(AnimationType.LOOK_UP)
        }
    }

    timedAction(victoryTextDuration) {
        onStart = {
            gameViewModel.onShowMessage("avatar-fox", victoryText)
        }
    }

    action {
        onStart = {
            gameViewModel.onHideMessage()
            player().run {
                val (position) = this[Transform]
                this.configure {
                    it += MoveTo(gdxArrayOf(MoveToPoint(vec2(position.x + 10f, position.y), Interpolation.linear, 3f)))
                }
                this[Animation].changeTo(AnimationType.RUN)
            }
        }
    }

    action {
        onStart = {
            entity {
                it += Transition(
                    gdxArrayOf(TransitionEffect(TransitionType.CIRCLE_CROP, duration = 2.5f, reversed = true))
                )
                it += player()[Transform]
            }
        }
    }
}