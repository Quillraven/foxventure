package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.MusicAsset
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.get
import io.github.quillraven.foxventure.system.RenderSystem
import ktx.app.KtxScreen
import ktx.math.vec2

class GameScreen(
    game: GdxGame,
    private val batch: Batch = game.serviceLocator.batch,
    private val assets: AssetManager = game.serviceLocator.assets,
    private val gameViewport: Viewport = game.gameViewport,
    private val stage: Stage = game.stage,
) : KtxScreen {

    private val world = ecsWorld()

    private fun ecsWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(stage)
        }

        systems {
            add(RenderSystem())
        }
    }

    override fun show() {
        assets[MusicAsset.HURT_AND_HEART].play()

        world.entity {
            it += Transform(
                position = vec2(0f, 0f),
                size = vec2(2.0625f, 2f),
                rotationDegrees = 0f,
                scale = 1f,
            )
            it += Graphic(assets[AtlasAsset.CHARACTERS].findRegions("fox/idle").first())
            it += EntityTag.ACTIVE
        }
    }

    override fun render(delta: Float) {
        world.update(delta)
    }
}