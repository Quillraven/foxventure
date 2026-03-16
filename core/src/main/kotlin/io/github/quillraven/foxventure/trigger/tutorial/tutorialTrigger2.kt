package io.github.quillraven.foxventure.trigger.tutorial

import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.trigger.trigger

fun IntervalSystem.tutorialTrigger2() = trigger {
    timedAction(0.5f) {
        onStart = {
            player().run {
                configure {
                    it += EntityTag.ROOT
                    it -= Controller
                }
                this[Velocity].current.setZero()
            }
        }
    }

    timedAction(13f) {
        onStart = {
            gameViewModel.onShowMessage(
                "avatar-fox",
                "A ladder? Really? {WAIT=0.5}Talk about a workout.\n" +
                        "I used to be a master of the vertical scramble.\n" +
                        "Let's see if I can still climb without falling on my tail!"
            )
        }
    }

    timedAction(5f) {
        onStart = {
            gameViewModel.onShowMessage(
                "",
                "Press {VAR=HIGHLIGHT}W/D{VAR=END_HIGHLIGHT} or {VAR=HIGHLIGHT}UP/DOWN{VAR=END_HIGHLIGHT} arrow keys to climb ladders."
            )
        }
    }

    action {
        onStart = {
            gameViewModel.onHideMessage()
            player().configure {
                it += Controller()
                it -= EntityTag.ROOT
            }
        }
    }
}
