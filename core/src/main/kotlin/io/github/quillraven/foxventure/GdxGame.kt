package io.github.quillraven.foxventure

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.quillraven.foxventure.screen.LoadAssetsScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GdxGame : KtxGame<KtxScreen>() {
    val serviceLocator: ServiceLocator by lazy { ServiceLocator() }
    val gameViewport: Viewport = ExtendViewport(16f, 9f)
    val uiViewport: Viewport = ScreenViewport()
    val stage: Stage by lazy { Stage(uiViewport, serviceLocator.batch) }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        addScreen(LoadAssetsScreen(this))
        setScreen<LoadAssetsScreen>()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiViewport.update(width, height, true)
        super.resize(width, height)
    }

    override fun dispose() {
        super.dispose()
        stage.dispose()
        serviceLocator.dispose()
    }

    companion object {
        fun Int.toWorldUnits() = this / 16f
    }
}