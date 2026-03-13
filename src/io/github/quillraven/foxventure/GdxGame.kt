package io.github.quillraven.foxventure

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ScreenUtils
import com.ray3k.stripe.FreeTypeSkin
import io.github.quillraven.foxventure.screen.GameScreen
import io.github.quillraven.foxventure.screen.WebStartScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GdxGame(val isWeb: Boolean) : KtxGame<KtxScreen>() {
    val serviceLocator: ServiceLocator by lazy { ServiceLocator(InternalFileHandleResolver()) }
    val stage: Stage by lazy { Stage(serviceLocator.renderContext.uiViewport, serviceLocator.renderContext.batch) }
    val skin: Skin by lazy {
        val resolver = serviceLocator.fileHandleResolver
        val atlas = TextureAtlas(resolver.resolve("ui/ui.atlas"))
        FreeTypeSkin(resolver.resolve("ui/ui.json"), atlas)
    }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        if (isWeb) {
            addScreen(WebStartScreen(this))
            setScreen<WebStartScreen>()
        } else {
            addScreen(GameScreen(this))
            setScreen<GameScreen>()
        }
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
        skin.dispose()
    }

    companion object {
        fun Int.toWorldUnits() = this / 16f

        fun Float.toWorldUnits() = this / 16f
    }
}