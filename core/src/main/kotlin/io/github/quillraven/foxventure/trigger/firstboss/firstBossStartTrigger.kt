package io.github.quillraven.foxventure.trigger.firstboss

import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.ai.FrogBossStateJump
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.trigger.trigger

fun IntervalSystem.firstBossStartTrigger() = trigger {
    timedAction(0.5f) {
        onStart = {
            player().run {
                configure {
                    it += EntityTag.ROOT
                    it -= Controller
                }
                this[Velocity].current.setZero()
            }
            audioService.stopMusic()
        }
    }

    timedAction(17f) {
        onStart = {
            gameViewModel.onShowMessage(
                "avatar-fox",
                "{SHAKE}Yip!{WAIT=0.6}{RESET} That is one... [*]big[*]... lily-pad hopper.\n" +
                        "My instincts are telling me to pounce, but my eyes are telling me to run!\n" +
                        "Alright, Ribbit-face, {WAIT=0.5}get out of the way. I've got a dinner date with my wife!"
            )
        }
    }

    action {
        onStart = {
            val boss = this@trigger.world.family { all(EntityTag.BOSS) }.single()
            boss[Fsm].state.changeState(FrogBossStateJump)
            audioService.playMusic("boss.mp3")
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