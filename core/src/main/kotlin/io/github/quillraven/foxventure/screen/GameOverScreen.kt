package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.quillraven.foxventure.AudioService
import io.github.quillraven.foxventure.GdxGame
import ktx.app.KtxScreen
import ktx.graphics.use
import kotlin.math.sin

class GameOverScreen(
    private val game: GdxGame,
    private val batch: Batch = game.serviceLocator.renderContext.batch,
    private val uiViewport: Viewport = game.serviceLocator.renderContext.uiViewport,
    private val audioService: AudioService = game.serviceLocator.audioService,
    skin: Skin = game.skin,
) : KtxScreen {
    private val font = skin.getFont("border")
    private val titleLayout = GlyphLayout().also { it.setText(font, "Game Over") }
    private val tapLayout = GlyphLayout()
    private val background = Texture("graphics/game_over.jpg").apply {
        setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }
    private var blinkTimer = 0f

    override fun render(delta: Float) {
        blinkTimer += delta

        uiViewport.apply()
        batch.use(uiViewport.camera) {
            batch.draw(background, 0f, 0f, uiViewport.worldWidth, uiViewport.worldHeight)

            // "Game Over" slightly above the center
            val centerX = uiViewport.worldWidth / 2f
            val centerY = uiViewport.worldHeight / 2f
            val gap = 20f
            font.draw(batch, titleLayout, centerX - titleLayout.width / 2f, centerY + titleLayout.height + gap)

            // "Tap" text below with smooth fade in/out
            // Important to set text each frame because otherwise the color change doesn't work
            val alpha = (sin((blinkTimer * Math.PI)) * 0.5 + 0.5).toFloat()
            font.setColor(1f, 1f, 1f, alpha)
            tapLayout.setText(font, "Tap to return to main menu")
            font.draw(batch, tapLayout, centerX - tapLayout.width / 2f, centerY - gap)
        }

        if (Gdx.input.isTouched) {
            game.setScreen<MainMenuScreen>()
            return
        }
    }

    override fun show() {
        blinkTimer = 0f
        audioService.playMusic("game_over_loop.ogg")
    }

    override fun dispose() {
        background.dispose()
    }
}
