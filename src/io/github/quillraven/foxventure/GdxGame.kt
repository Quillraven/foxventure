package io.github.quillraven.foxventure

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ScreenUtils
import com.ray3k.stripe.FreeTypeSkinLoader
import io.github.quillraven.foxventure.screen.LoadAssetsScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.setLoader

class GdxGame : KtxGame<KtxScreen>() {
    val serviceLocator: ServiceLocator by lazy { ServiceLocator() }
    val stage: Stage by lazy { Stage(serviceLocator.renderContext.uiViewport, serviceLocator.renderContext.batch) }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        with(serviceLocator.assets) {
            val tiledLoader = TmxMapLoader(this.fileHandleResolver)
            this.setLoader<TiledMap, BaseTiledMapLoader.Parameters>(tiledLoader)
            this.setLoader<Skin, SkinLoader.SkinParameter>(FreeTypeSkinLoader(this.fileHandleResolver))
        }

        addScreen(LoadAssetsScreen(this))
        setScreen<LoadAssetsScreen>()
    }

    override fun resize(width: Int, height: Int) {
        serviceLocator.renderContext.resize(width, height)
        super.resize(width, height)
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f, true)
        val deltaTime = Gdx.graphics.deltaTime.coerceIn(0f, 1 / 30f)
        currentScreen.render(deltaTime)
        serviceLocator.audioService.update(deltaTime)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }
    }

    override fun dispose() {
        super.dispose()
        stage.dispose()
        serviceLocator.dispose()
    }

    companion object {
        fun Int.toWorldUnits() = this / 16f

        fun Float.toWorldUnits() = this / 16f
    }
}