package io.github.quillraven.foxventure.trigger.tutorial

import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.trigger.trigger

fun IntervalSystem.tutorialTrigger1() = trigger {
    timedAction(0.5f) {
        onStart = {
            player().run {
                configure {
                    it += EntityTag.ROOT
                    it -= Controller.Companion
                }
                this[Velocity.Companion].current.setZero()
            }
        }
    }

    timedAction(15f) {
        onStart = {
            gameViewModel.onShowMessage(
                "avatar-fox",
                "{SHAKE}Ruff!{WAIT=1.0}{RESET} That fall nearly ruffled my fur for good!\n" +
                        "Wait... I feel a spring in my paws. I used to be able to catch some air, didn't I?\n" +
                        "How did I... pounce?"
            )
        }
    }

    timedAction(4f) {
        onStart = {
            gameViewModel.onShowMessage(
                "",
                "Press {VAR=HIGHLIGHT}SPACE{VAR=END_HIGHLIGHT} to jump."
            )
        }
    }

    action {
        onStart = {
            gameViewModel.onHideMessage()
            player().configure {
                it += Controller()
                it += JumpControl()
                it -= EntityTag.ROOT
            }
        }
    }
}