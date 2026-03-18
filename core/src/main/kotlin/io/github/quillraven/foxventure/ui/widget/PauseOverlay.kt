package io.github.quillraven.foxventure.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable

class PauseOverlay(skin: Skin) : Stack(), Disposable {
    private val whiteTexture: Texture

    init {
        setFillParent(true)

        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE)
            fill()
        }
        whiteTexture = Texture(pixmap)
        pixmap.dispose()

        val background = Image(whiteTexture).apply {
            setColor(0f, 0f, 0f, 0.5f)
        }

        val label = Label("PAUSE", skin).apply {
            addAction(
                Actions.forever(
                    Actions.sequence(
                        Actions.fadeOut(1.1f),
                        Actions.fadeIn(1.1f),
                    )
                )
            )
        }

        val labelTable = Table().apply {
            add(label)
        }

        add(background)
        add(labelTable)
    }

    override fun act(delta: Float) {
        super.act(delta.coerceAtLeast(1 / 30f))
    }

    override fun dispose() {
        whiteTexture.dispose()
    }
}
