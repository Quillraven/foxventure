package io.github.quillraven.foxventure.trigger.firstboss

import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.screen.VictoryScreen
import io.github.quillraven.foxventure.trigger.trigger

fun firstBossDeathTrigger(world: World) = trigger(world) {
    timedAction(3f) {
        onStart = {
            player().run {
                configure {
                    it += EntityTag.ROOT
                    it -= Controller
                }
                this[Velocity].current.setZero()
            }
            audioService.stopMusic()
            audioService.playSound("croak4.wav")
        }
    }

    timedAction(14f) {
        onStart = {
            gameViewModel.onShowMessage(
                "avatar-fox",
                "Down goes the big hopper! {WAIT=0.6}Now, if I can just find that honey-glazed salmon...\n" +
                        "Wait. {WAIT=0.8}I feel a strange disturbance in the pixels. Like the world just... stops here?"
            )
        }
    }

    timedAction(5f) {
        onStart = {
            gameViewModel.onShowMessage(
                "",
                "{VAR=HIGHLIGHT}Thank you for playing the Foxventure demo!{VAR=END_HIGHLIGHT}"
            )
        }
    }

    action {
        onStart = {
            game.setScreen<VictoryScreen>()
        }
    }
}