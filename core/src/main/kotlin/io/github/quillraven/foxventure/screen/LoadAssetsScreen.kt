package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.assets.AssetManager
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.MusicAsset
import ktx.app.KtxScreen
import ktx.assets.loadAsset

class LoadAssetsScreen(
    private val game: GdxGame,
    private val assets: AssetManager = game.serviceLocator.assets,
) : KtxScreen {

    override fun show() {
        MusicAsset.entries.forEach { assets.loadAsset(it.descriptor) }
        AtlasAsset.entries.forEach { assets.loadAsset(it.descriptor) }
    }

    override fun render(delta: Float) {
        if (assets.update()) {
            dispose()
            game.removeScreen<LoadAssetsScreen>()
            game.addScreen(GameScreen(game))
            game.setScreen<GameScreen>()
            return
        }
    }
}