package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.MapAsset
import io.github.quillraven.foxventure.system.ActivationSystem
import io.github.quillraven.foxventure.system.AerialMoveSystem
import io.github.quillraven.foxventure.system.AnimationSystem
import io.github.quillraven.foxventure.system.AttackSystem
import io.github.quillraven.foxventure.system.AudioService
import io.github.quillraven.foxventure.system.CameraSystem
import io.github.quillraven.foxventure.system.ClimbSystem
import io.github.quillraven.foxventure.system.CollisionSystem
import io.github.quillraven.foxventure.system.ControllerSystem
import io.github.quillraven.foxventure.system.DamageRequestSystem
import io.github.quillraven.foxventure.system.DamagedSystem
import io.github.quillraven.foxventure.system.DelayRemovalSystem
import io.github.quillraven.foxventure.system.FollowSystem
import io.github.quillraven.foxventure.system.FsmSystem
import io.github.quillraven.foxventure.system.GroundMoveSystem
import io.github.quillraven.foxventure.system.LifeSystem
import io.github.quillraven.foxventure.system.PhysicsTimer
import io.github.quillraven.foxventure.system.PostInterpolationSystem
import io.github.quillraven.foxventure.system.PreInterpolationSystem
import io.github.quillraven.foxventure.system.ProximityDetectorSystem
import io.github.quillraven.foxventure.system.RenderSystem
import io.github.quillraven.foxventure.system.SpawnSystem
import io.github.quillraven.foxventure.system.WanderSystem
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
    private val audioService: AudioService = game.serviceLocator.audioService,
) : KtxScreen {

    private val physicsTimer = PhysicsTimer(interval = 1 / 60f)
    private val world = ecsWorld()

    private fun ecsWorld() = configureWorld {
        injectables {
            add(batch)
            add(gameViewport)
            add(stage)
            add(assets)
            add(tiledService)
            add(physicsTimer)
            add(audioService)
        }

        systems {
            add(ControllerSystem())
            add(SpawnSystem())
            add(ActivationSystem())
            add(PreInterpolationSystem()) // run it before any physics system runs (climb, aerial, ground)
            add(ProximityDetectorSystem())
            add(FollowSystem())
            add(WanderSystem())
            add(AttackSystem())
            add(ClimbSystem())
            add(AerialMoveSystem())
            add(GroundMoveSystem())
            add(CollisionSystem())
            add(DamagedSystem())
            add(LifeSystem())
            add(FsmSystem())
            add(DamageRequestSystem())
            add(CameraSystem())
            add(AnimationSystem())
            add(PostInterpolationSystem()) // run it after all physics systems run
            add(RenderSystem())
            add(DelayRemovalSystem())
//            add(DebugRenderSystem())
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = InputMultiplexer(stage, world.system<ControllerSystem>())

        registerTiledListeners()
        tiledService.setMap(MapAsset.TUTORIAL)
    }

    override fun render(delta: Float) {
        physicsTimer.update(delta)
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
        tiledService.addMapChangeListener(audioService)
    }

    override fun dispose() {
        world.dispose()
    }
}