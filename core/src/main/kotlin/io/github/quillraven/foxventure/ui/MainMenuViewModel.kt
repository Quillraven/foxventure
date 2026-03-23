package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.Gdx
import io.github.quillraven.foxventure.AudioService
import io.github.quillraven.foxventure.GdxGame

class MainMenuViewModel(
    private val game: GdxGame,
    private val audioService: AudioService = game.serviceLocator.audioService,
) {
    var musicVolume: Float by notifying(audioService.musicVolume) { onMusicVolumeChanged(it.coerceIn(0f, 1f)) }
    var soundVolume: Float by notifying(audioService.soundVolume) { onSoundVolumeChanged(it.coerceIn(0f, 1f)) }

    var onMusicVolumeChanged: (volume: Float) -> Unit = { volume -> audioService.musicVolume = volume }
    var onSoundVolumeChanged: (volume: Float) -> Unit = { volume -> audioService.soundVolume = volume }
    var onStartGame: () -> Unit = { game.changeToGame("tutorial.tmx") }
    var onQuit: () -> Unit = { Gdx.app.exit() }
}
