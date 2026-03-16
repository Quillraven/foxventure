package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import io.github.quillraven.foxventure.component.TriggerRef
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.component.Victory
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.trigger.trigger
import ktx.collections.gdxArrayOf
import ktx.math.vec2

class PlayerVictorySystem : IteratingSystem(
    family = family { all(Player, Victory, Controller, EntityTag.ACTIVE) }
), MapChangeListener {
    private var victoryText = ""
    private var victoryTextDuration = 0f

    override fun onTickEntity(entity: Entity) {
        entity.configure {
            it -= Controller
        }
        world.entity {
            it += TriggerRef(victoryTrigger(entity))
        }
    }

    private fun victoryTrigger(player: Entity) = trigger {
        timedAction(1f) {
            onStart = {
                audioService.fadeOutMusic(2f)
            }

            onUpdate = {
                MathUtils.isEqual(player[Velocity].current.x, 0f, 0.01f)
            }
        }

        action {
            onStart = {
                val houseCenter = player[Victory].housePosition
                player.configure {
                    it -= Physics
                    it += MoveTo(gdxArrayOf(MoveToPoint(houseCenter, Interpolation.linear, 1f)))
                }
                player[Animation].changeTo(AnimationType.RUN)
            }
            onUpdate = { player hasNo MoveTo }
        }

        timedAction(2f) {
            onStart = {
                audioService.playMusic("8-bit-on-short.mp3")
                player[Animation].changeTo(AnimationType.LOOK_UP)
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
                val (position) = player[Transform]
                player.configure {
                    it += MoveTo(gdxArrayOf(MoveToPoint(vec2(position.x + 10f, position.y), Interpolation.linear, 3f)))
                }
                player[Animation].changeTo(AnimationType.RUN)
            }
        }

        action {
            onStart = {
                entity {
                    it += Transition(
                        gdxArrayOf(TransitionEffect(TransitionType.CIRCLE_CROP, duration = 2.5f, reversed = true))
                    )
                    it += player[Transform]
                }
            }
        }
    }

    override fun onMapChanged(mapName: String, tiledMap: TiledMap) {
        when (mapName) {
            "tutorial.tmx" -> {
                victoryText = "{SHAKE}Sniff, sniff...{WAIT=0.8}{RESET} Is that... honey-glazed salmon? My favorite!\n" +
                        "My tail is wagging just thinking about my wife's cooking. I’m almost home!{WAIT=1.0}\n" +
                        "Wait. {WAIT=0.5}This isn't my den.\n" +
                        "{COLOR=#87ceebff}Thank you, but my house is in another level.{RESET}"
                victoryTextDuration = 20f
            }

            else -> {
                victoryText = "Haha! I made it! {WAIT=0.5}Honey, I'm ho—{WAIT=0.8}\n" +
                        "...Oh. {SHAKE}Wrong chimney.{RESET} No wonder the welcome mat looked different.\n" +
                        "{COLOR=#87ceebff}Thank you, but my house is in another level.{RESET}"
                victoryTextDuration = 15f
            }
        }
    }
}
