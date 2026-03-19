package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.tommyettinger.textra.TypingLabel
import io.github.quillraven.foxventure.GdxGame
import ktx.app.KtxScreen
import ktx.graphics.use
import kotlin.math.sin

class VictoryScreen(
    private val game: GdxGame,
    private val batch: Batch = game.serviceLocator.renderContext.batch,
    private val uiViewport: Viewport = game.serviceLocator.renderContext.uiViewport,
    skin: Skin = game.skin,
) : KtxScreen {
    private val font = skin.getFont("border")
    private val tapLayout = GlyphLayout()
    private val background = Texture("graphics/victory.jpg").apply {
        setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }
    private var blinkTimer = 0f
    private val victoryLabel = TypingLabel("[%375]{RAINBOW}{ARC=0.75}Victory[%]", skin, "border").apply {
        skipToTheEnd()
        pack()
    }

    override fun render(delta: Float) {
        blinkTimer += delta

        uiViewport.apply()
        batch.use(uiViewport.camera) {
            batch.draw(background, 0f, 0f, uiViewport.worldWidth, uiViewport.worldHeight)

            val centerX = uiViewport.worldWidth / 2f
            val centerY = uiViewport.worldHeight / 2f
            val gap = 30f

            // "Victory" with rainbow effect, slightly above center
            victoryLabel.setPosition(centerX - victoryLabel.width / 2f, centerY + victoryLabel.height + gap)
            victoryLabel.act(delta)
            victoryLabel.draw(batch, 1f)

            // "Tap" text below with smooth fade in/out
            val alpha = (sin((blinkTimer * Math.PI)) * 0.5 + 0.5).toFloat()
            font.setColor(1f, 1f, 1f, alpha)
            tapLayout.setText(font, "Tap to return to main menu")
            font.draw(batch, tapLayout, centerX - tapLayout.width / 2f, centerY - gap)
        }

        if (Gdx.input.isTouched) {
            game.changeToGame("tutorial.tmx")
            return
        }
    }

    override fun show() {
        blinkTimer = 0f
    }

    override fun dispose() {
        background.dispose()
    }
}
