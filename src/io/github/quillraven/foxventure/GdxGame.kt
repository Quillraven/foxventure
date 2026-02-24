package io.github.quillraven.foxventure

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.quillraven.foxventure.screen.LoadAssetsScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.setLoader

class GdxGame : KtxGame<KtxScreen>() {
    val serviceLocator: ServiceLocator by lazy { ServiceLocator() }
    val gameViewport: Viewport = ExtendViewport(16f, 9f)
    val uiViewport: Viewport = ScreenViewport()
    val stage: Stage by lazy { Stage(uiViewport, serviceLocator.batch) }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        val tiledLoader = TmxMapLoader(serviceLocator.assets.fileHandleResolver)
        serviceLocator.assets.setLoader<TiledMap, BaseTiledMapLoader.Parameters>(tiledLoader)

        addScreen(LoadAssetsScreen(this))
        setScreen<LoadAssetsScreen>()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)
        super.resize(width, height)
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f, true)
        val deltaTime = Gdx.graphics.deltaTime.coerceIn(0f, 1 / 30f)
        currentScreen.render(deltaTime)
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