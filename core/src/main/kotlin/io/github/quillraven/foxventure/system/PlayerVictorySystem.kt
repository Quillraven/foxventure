package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Tiled
import io.github.quillraven.foxventure.component.TriggerRef
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Victory
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.trigger.victoryTrigger

class PlayerVictorySystem : IteratingSystem(
    family = family { all(Player, Victory, Controller, EntityTag.ACTIVE) }
), MapChangeListener {
    private var victoryText = ""
    private var victoryTextDuration = 0f
    private var gems = 0
    private var gemsTotal = 0
    private var mapName = ""
    private val gemsFamily = world.family { all(Type, Tiled) }

    override fun onTickEntity(entity: Entity) {
        // remove player control
        entity.configure {
            it -= Controller
        }

        // count the number of collected gems
        val gemsLeft = gemsFamily.filter { it[Type].type == "gem" }.count()
        gems = gemsTotal - gemsLeft

        // create a victory trigger for level complete cutscene
        world.entity {
            it += TriggerRef(victoryTrigger(victoryTextDuration, victoryText, gems, gemsTotal, mapName))
        }
    }

    override fun onMapChanged(mapName: String, tiledMap: TiledMap) {
        gemsTotal = gemsFamily.filter { it[Type].type == "gem" }.count()
        gems = 0
        this.mapName = mapName.substringBeforeLast(".").uppercase()

        when (mapName) {
            "tutorial.tmx" -> {
                victoryText = "{SHAKE}Sniff, sniff...{WAIT=0.8}{RESET} Is that... honey-glazed salmon? My favorite!\n" +
                        "My tail is wagging just thinking about my wife's cooking. I’m almost home!{WAIT=1.0}\n" +
                        "Wait. {WAIT=0.5}This isn't my den.\n" +
                        "{VAR=HIGHLIGHT}Thank you, but my house is in another level.{VAR=END_HIGHLIGHT}"
                victoryTextDuration = 20f
            }

            else -> {
                victoryText = "Haha! I made it! {WAIT=0.5}Honey, I'm ho—{WAIT=0.8}\n" +
                        "...Oh. {SHAKE}Wrong chimney.{RESET} No wonder the welcome mat looked different.\n" +
                        "{VAR=HIGHLIGHT}Thank you, but my house is in another level.{VAR=END_HIGHLIGHT}"
                victoryTextDuration = 15f
            }
        }
    }
}
