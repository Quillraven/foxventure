package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.quillraven.foxventure.AudioService
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.ui.MainMenuView
import io.github.quillraven.foxventure.ui.MainMenuViewModel
import ktx.app.KtxScreen
import ktx.graphics.use

class MainMenuScreen(
    private val game: GdxGame,
    private val stage: Stage = game.stage,
    private val uiViewport: Viewport = game.serviceLocator.renderContext.uiViewport,
    private val audioService: AudioService = game.serviceLocator.audioService,
) : KtxScreen {
    private val background = Texture("graphics/menu.jpg").apply {
        setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }
    private val viewModel = MainMenuViewModel(game)

    override fun show() {
        Gdx.input.inputProcessor = stage
        audioService.playMusic("fantasy_dragon.ogg")

        stage.clear()
        stage.addActor(MainMenuView(viewModel, game.skin, game.isWeb))
    }

    override fun render(delta: Float) {
        uiViewport.apply()
        stage.batch.use(uiViewport.camera) {
            it.draw(background, 0f, 0f, uiViewport.worldWidth, uiViewport.worldHeight)
        }
        stage.act()
        stage.draw()
    }

    override fun dispose() {
        background.dispose()
    }
}
