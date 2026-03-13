package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import io.github.quillraven.foxventure.GdxGame
import ktx.app.KtxScreen

class WebStartScreen(
    private val game: GdxGame,
) : KtxScreen {
    override fun render(delta: Float) {
        if (Gdx.input.isTouched) {
            dispose()
            game.removeScreen<WebStartScreen>()
            game.addScreen(GameScreen(game))
            game.setScreen<GameScreen>()
            return
        }
    }
}