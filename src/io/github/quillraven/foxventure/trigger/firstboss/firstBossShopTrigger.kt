package io.github.quillraven.foxventure.trigger.firstboss

import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.trigger.trigger

fun IntervalSystem.firstBossShopTrigger() = trigger {
    timedAction(1f) {
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

    timedAction(14f) {
        onStart = {
            gameViewModel.onShowMessage(
                "avatar-fox",
                "Well, look at that! {WAIT=0.6}A friendly face in a damp place.\n" +
                        "I remember him now — he is the local merchant.\n" +
                        "I'd better grab some supplies before I go any further."
            )
        }
    }

    timedAction(4f) {
        onStart = {
            gameViewModel.onShowMessage(
                "",
                "Press {VAR=HIGHLIGHT}W{VAR=END_HIGHLIGHT} or {VAR=HIGHLIGHT}UP{VAR=END_HIGHLIGHT} arrow key to open the shop."
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