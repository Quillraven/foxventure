package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.trigger.Trigger
import io.github.quillraven.foxventure.trigger.tutorialCutscene

class TriggerSystem : IntervalSystem(), MapChangeListener {
    private val activeTriggers = mutableListOf<Trigger>()

    override fun onTick() {
        activeTriggers.removeAll { trigger ->
            trigger.update(world)
            trigger.isDone
        }
    }

    override fun onMapChanged(mapName: String, tiledMap: TiledMap) {
        when (mapName) {
            "tutorial.tmx" -> activeTriggers += tutorialCutscene()
        }
    }
}
