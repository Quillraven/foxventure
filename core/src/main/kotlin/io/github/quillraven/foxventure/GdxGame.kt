package io.github.quillraven.foxventure

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ScreenUtils
import com.github.tommyettinger.freetypist.FreeTypistSkin
import com.github.tommyettinger.textra.TypingConfig
import io.github.quillraven.foxventure.screen.GameScreen
import io.github.quillraven.foxventure.screen.WebStartScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class GdxGame(val isWeb: Boolean) : KtxGame<KtxScreen>() {
    val serviceLocator: ServiceLocator by lazy { ServiceLocator(InternalFileHandleResolver()) }
    val stage: Stage by lazy { Stage(serviceLocator.renderContext.uiViewport, serviceLocator.renderContext.batch) }
    val skin: Skin by lazy {
        TypingConfig.DEFAULT_SPEED_PER_CHAR = 0.07f

        TypingConfig.GLOBAL_VARS.put("HIGHLIGHT", "{COLOR=#87ceebff}")
        TypingConfig.GLOBAL_VARS.put("END_HIGHLIGHT", "{ENDCOLOR}")

        val resolver = serviceLocator.fileHandleResolver
        val atlas = TextureAtlas(resolver.resolve("ui/ui.atlas"))
        FreeTypistSkin(resolver.resolve("ui/ui.json"), atlas)
    }

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        if (isWeb) {
            addScreen(WebStartScreen(this))
            setScreen<WebStartScreen>()
        } else {
            changeToGame("tutorial.tmx")
        }
    }

    fun changeToGame(mapName: String) {
        Gdx.app.log("GdxGame", "Change to game map $mapName")
        val prevGameScreen = screens[GameScreen::class.java]
        removeScreen<GameScreen>()
        val gameScreen = GameScreen(this)
        addScreen(gameScreen)
        setScreen<GameScreen>()
        prevGameScreen?.dispose()
        gameScreen.setMap(mapName)
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