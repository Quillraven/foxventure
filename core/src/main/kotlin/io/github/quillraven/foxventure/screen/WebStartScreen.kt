package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.quillraven.foxventure.GdxGame
import ktx.app.KtxScreen
import ktx.graphics.use

class WebStartScreen(
    private val game: GdxGame,
    private val batch: Batch = game.serviceLocator.renderContext.batch,
    private val uiViewport: Viewport = game.serviceLocator.renderContext.uiViewport,
    skin: Skin = game.skin,
) : KtxScreen {
    private val font = skin.getFont("small_border")
    private val layout = GlyphLayout()

    override fun render(delta: Float) {
        uiViewport.apply()
        batch.use(uiViewport.camera) {
            layout.setText(font, "Tap to start")
            val centerX = uiViewport.worldWidth / 2
            val centerY = uiViewport.worldHeight / 2
            font.draw(batch, layout, centerX - layout.width / 2, centerY + layout.height / 2)
        }

        if (Gdx.input.isTouched) {
            dispose()
            game.setScreen<MainMenuScreen>()
            return
        }
    }
}