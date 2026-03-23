package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
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
            setScale(0.75f)
        }

        val startButton = TextButton("Start Game", skin)
        startButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) = viewModel.onStartGame()
        })
        content.add(startButton).row()

        content.add(sliderBlock("Music Volume", skin, viewModel.musicVolume) { viewModel.musicVolume = it }).row()
        content.add(sliderBlock("Sound Volume", skin, viewModel.soundVolume) { viewModel.soundVolume = it }).row()

        if (!isWeb) {
            val quitButton = TextButton("Quit Game", skin)
            quitButton.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) = viewModel.onQuit()
            })
            content.add(quitButton).row()
        }

        add(content).center().padBottom(20f)
    }

    private fun sliderBlock(title: String, skin: Skin, initialValue: Float, onChange: (Float) -> Unit): Table {
        val block = Table(skin)

        block.add(Label(title, skin, "border")).left().padBottom(5f).row()

        val slider = VolumeSlider(skin, initialValue)
        slider.onValueChanged = onChange
        block.add(slider).fillX().row()

        val minLabel = Label("0", skin, "small_border")
        val maxLabel = Label("100", skin, "small_border")
        block.add(Table(skin).also { row ->
            row.add(minLabel).left().expandX().padTop(5f).padLeft(2f)
            row.add(maxLabel).right().padTop(5f).padLeft(20f)
        }).fillX().row()

        return block
    }
}
