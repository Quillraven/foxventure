package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.trigger.onTutorialMapLoad

class TriggerSystem : IntervalSystem(enabled = false), MapChangeListener {
    override fun onTick() = Unit

    override fun onMapChanged(mapName: String, tiledMap: TiledMap) {
        when (mapName) {
            "tutorial.tmx" -> onTutorialMapLoad()
        }
    }
}