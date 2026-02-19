package io.github.quillraven.foxventure.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.tiled.MapChangeListener
import ktx.assets.toInternalFile
import ktx.tiled.property

class AudioSystem : IntervalSystem(), MapChangeListener {
    private var currentMusic: Music? = null

    override fun onTick() = Unit

    private fun playMusic(name: String) {
        // dispose of current music if there is any
        currentMusic?.let { music ->
            music.stop()
            music.dispose()
        }

        // load and play new music
        val music = Gdx.audio.newMusic("music/$name".toInternalFile())
        music.isLooping = true
        music.play()
        currentMusic = music
    }

    override fun onMapChanged(tiledMap: TiledMap) {
        val musicPath = tiledMap.property("music", "").substringAfterLast("/")
        if (musicPath.isBlank()) return

        playMusic(musicPath)
    }

    override fun onDispose() {
        currentMusic?.dispose()
    }
}