package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.MapAsset
import io.github.quillraven.foxventure.system.ActivationSystem
import io.github.quillraven.foxventure.system.AnimationSystem
import io.github.quillraven.foxventure.system.AudioSystem
import io.github.quillraven.foxventure.system.CameraSystem
import io.github.quillraven.foxventure.system.ControllerSystem
import io.github.quillraven.foxventure.system.FsmSystem
import io.github.quillraven.foxventure.system.MoveSystem
import io.github.quillraven.foxventure.system.RenderSystem
import io.github.quillraven.foxventure.system.SpawnSystem
import io.github.quillraven.foxventure.tiled.LoadTileObjectListener
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.tiled.TiledService
import ktx.app.KtxScreen

class GameScreen(
    game: GdxGame,
    private val batch: Batch = game.serviceLocator.batch,
    private val assets: AssetManager = game.serviceLocator.assets,
    private val gameViewport: Viewport = game.gameViewport,
    private val stage: Stage = game.stage,
    private val tiledService: TiledService = game.serviceLocator.tiledService,
) : KtxScreen {

    private val world = ecsWorld()

    private fun ecsWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(stage)
            add(assets)
            add(tiledService)
        }

        systems {
            add(ControllerSystem())
            add(SpawnSystem())
            add(ActivationSystem())
            add(FsmSystem())
            add(MoveSystem())
            add(CameraSystem())
            add(AnimationSystem())
            add(RenderSystem())
//            add(DebugRenderSystem())
            add(AudioSystem())
        }
    }

    override fun show() {
        registerTiledListeners()
        tiledService.setMap(MapAsset.TUTORIAL)
    }

    override fun render(delta: Float) {
        world.update(delta)
    }

    private fun registerTiledListeners() {
        world.systems.forEach { system ->
            if (system is MapChangeListener) {
                tiledService.addMapChangeListener(system)
            }
            if (system is LoadTileObjectListener) {
                tiledService.addLoadTileObjectListener(system)
            }
        }
    }

    override fun dispose() {
        world.dispose()
    }
}