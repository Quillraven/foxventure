package io.github.quillraven.foxventure.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.utils.Disposable
import io.github.quillraven.foxventure.tiled.MapChangeListener
import ktx.assets.toInternalFile
import ktx.tiled.property

class AudioService : MapChangeListener, Disposable {
    var musicVolume = 0.3f
        set(value) {
            field = value.coerceIn(0f, 1f)
            currentMusic?.volume = field
        }

    var soundVolume = 1f

    private var currentMusic: Music? = null
    private val soundCache = mutableMapOf<String, Sound>()

    fun playSound(name: String) {
        if (soundCache.size > 100) {
            clearSoundCache()
        }

        val sound = soundCache.getOrPut(name) {
            Gdx.audio.newSound("sound/$name".toInternalFile())
        }
        sound.play(soundVolume)
    }

    private fun playMusic(name: String) {
        // dispose of current music if there is any
        currentMusic?.let { music ->
            music.stop()
            music.dispose()
        }

        // load and play new music
        Gdx.audio.newMusic("music/$name".toInternalFile())?.let { music ->
            music.isLooping = true
            music.play()
            music.volume = musicVolume
            currentMusic = music
        }
    }

    override fun onMapChanged(tiledMap: TiledMap) {
        val musicPath = tiledMap.property("music", "").substringAfterLast("/")
        if (musicPath.isBlank()) return

        playMusic(musicPath)
    }

    override fun dispose() {
        currentMusic?.dispose()
        clearSoundCache()
    }

    private fun clearSoundCache() {
        soundCache.values.forEach { it.dispose() }
        soundCache.clear()
    }
}