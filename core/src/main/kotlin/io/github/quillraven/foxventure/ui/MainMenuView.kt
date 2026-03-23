package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import io.github.quillraven.foxventure.ui.widget.VolumeSlider

class MainMenuView(
    private val viewModel: MainMenuViewModel,
    skin: Skin,
    isWeb: Boolean,
) : Table(skin) {

    init {
        setFillParent(true)
        bottom()

        val content = Table(skin).apply {
            background = skin.getDrawable("button-blue-gradient")
            pad(16f)
            defaults().padBottom(12f).fillX().minWidth(200f)
            isTransform = true
            setScale(0.85f)
        }

        val startButton = TextButton("Start Game", skin)
        startButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) = viewModel.onStartGame()
        })
        content.add(startButton).row()

        val musicSlider = VolumeSlider(skin, viewModel.musicVolume)
        musicSlider.onValueChanged = { viewModel.musicVolume = it }
        content.add(musicSlider).row()

        val soundSlider = VolumeSlider(skin, viewModel.soundVolume)
        soundSlider.onValueChanged = { viewModel.soundVolume = it }
        content.add(soundSlider).row()

        if (!isWeb) {
            val quitButton = TextButton("Quit Game", skin)
            quitButton.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) = viewModel.onQuit()
            })
            content.add(quitButton).row()
        }

        add(content).center().padBottom(20f)
    }
}
