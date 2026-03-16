package io.github.quillraven.foxventure

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
    var currentMusicName: String? = null

    private var tmpMusic: Music? = null
    private var tmpMusicTimer: Float = 0f

    private var fadeOutTimer: Float = 0f
    private var fadeOutDuration: Float = 0f

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

    fun playMusic(name: String) {
        // dispose of current music if there is any
        currentMusic?.let { music ->
            music.stop()
            music.dispose()
        }

        // load and play new music
        Gdx.audio.newMusic("music/$name".toInternalFile()).also { music ->
            music.isLooping = true
            music.play()
            music.volume = musicVolume
            currentMusic = music
            currentMusicName = name
        }
    }

    fun playTempMusic(name: String, duration: Float) {
        tmpMusicTimer = duration
        currentMusic?.pause()

        tmpMusic?.stop()
        tmpMusic?.dispose()
        tmpMusic = Gdx.audio.newMusic("music/$name".toInternalFile()).also { music ->
            music.isLooping = true
            music.play()
            music.volume = musicVolume
        }
    }

    fun stopMusic() {
        currentMusic?.stop()
        currentMusic?.dispose()
        currentMusic = null
    }

    fun fadeOutMusic(duration: Float) {
        if (currentMusic == null) return
        fadeOutDuration = duration
        fadeOutTimer = duration
    }

    override fun onMapChanged(mapName: String, tiledMap: TiledMap) {
        val musicPath = tiledMap.property("music", "").substringAfterLast("/")
        if (musicPath.isBlank()) return

        playMusic(musicPath)
    }

    fun update(deltaTime: Float) {
        if (fadeOutTimer > 0f) {
            fadeOutTimer -= deltaTime
            if (fadeOutTimer <= 0f) {
                stopMusic()
            } else {
                currentMusic?.volume = musicVolume * (fadeOutTimer / fadeOutDuration)
            }
        }

        if (tmpMusicTimer <= 0f) return

        tmpMusicTimer -= deltaTime
        if (tmpMusicTimer <= 0f) {
            tmpMusic?.stop()
            tmpMusic?.dispose()
            tmpMusic = null
            currentMusic?.play()
        }
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