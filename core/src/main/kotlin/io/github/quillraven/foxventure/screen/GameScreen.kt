package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.MapAsset
import io.github.quillraven.foxventure.SkinAsset
import io.github.quillraven.foxventure.component.DelayAction
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import io.github.quillraven.foxventure.graphic.RenderContext
import io.github.quillraven.foxventure.graphic.ShaderService
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
import io.github.quillraven.foxventure.system.DebugRenderSystem
import io.github.quillraven.foxventure.system.DelayActionSystem
import io.github.quillraven.foxventure.system.DelayRemovalSystem
import io.github.quillraven.foxventure.system.FlashSystem
import io.github.quillraven.foxventure.system.FollowSystem
import io.github.quillraven.foxventure.system.InvulnerabilitySystem
import io.github.quillraven.foxventure.system.FsmSystem
import io.github.quillraven.foxventure.system.GroundMoveSystem
import io.github.quillraven.foxventure.system.LifeSystem
import io.github.quillraven.foxventure.system.MoveToSystem
import io.github.quillraven.foxventure.system.PhysicsTimer
import io.github.quillraven.foxventure.system.PlayerDeathSystem
import io.github.quillraven.foxventure.system.PostInterpolationSystem
import io.github.quillraven.foxventure.system.PostRenderSystem
import io.github.quillraven.foxventure.system.PreInterpolationSystem
import io.github.quillraven.foxventure.system.ProximityDetectorSystem
import io.github.quillraven.foxventure.system.RenderSystem
import io.github.quillraven.foxventure.system.SpawnSystem
import io.github.quillraven.foxventure.system.StunSystem
import io.github.quillraven.foxventure.system.TransitionType
import io.github.quillraven.foxventure.system.UiRenderSystem
import io.github.quillraven.foxventure.system.WanderSystem
import io.github.quillraven.foxventure.tiled.LoadTileObjectListener
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.tiled.TiledService
import io.github.quillraven.foxventure.ui.GameView
import io.github.quillraven.foxventure.ui.GameViewModel
import ktx.app.KtxScreen
import ktx.collections.gdxArrayOf

class GameScreen(
    private val game: GdxGame,
    private val renderContext: RenderContext = game.serviceLocator.renderContext,
    private val assets: AssetManager = game.serviceLocator.assets,
    private val gameViewport: Viewport = game.gameViewport,
    private val stage: Stage = game.stage,
    private val tiledService: TiledService = game.serviceLocator.tiledService,
    private val audioService: AudioService = game.serviceLocator.audioService,
    private val shaderService: ShaderService = game.serviceLocator.shaderService,
) : KtxScreen {
    private val physicsTimer = PhysicsTimer(interval = 1 / 60f)
    private val gameViewModel = GameViewModel()
    private val world: World = ecsWorld()
    private val skin: Skin = assets[SkinAsset.UI]

    private fun ecsWorld() = configureWorld {
        injectables {
            add(renderContext)
            add(gameViewport)
            add(stage)
            add(assets)
            add(tiledService)
            add(physicsTimer)
            add(audioService)
            add(game)
            add(shaderService)
            add(gameViewModel)
        }

        val activationSystem = ActivationSystem(gameViewport)
        systems {
            add(ControllerSystem())
            add(SpawnSystem())
            add(activationSystem)
            add(PreInterpolationSystem()) // run it before any physics system runs (climb, aerial, ground)
            add(ProximityDetectorSystem())
            add(FollowSystem())
            add(WanderSystem())
            add(AttackSystem())
            add(MoveToSystem())
            add(ClimbSystem())
            add(AerialMoveSystem())
            add(GroundMoveSystem())
            add(CollisionSystem())
            add(DamagedSystem())
            add(InvulnerabilitySystem())
            add(StunSystem())
            add(LifeSystem())
            add(PlayerDeathSystem())
            add(FsmSystem())
            add(DamageRequestSystem())
            add(CameraSystem())
            add(AnimationSystem())
            add(FlashSystem())
            add(PostInterpolationSystem()) // run it after all physics systems run
            add(RenderSystem())
            add(PostRenderSystem())
            add(UiRenderSystem())
            add(DelayActionSystem())
            add(DelayRemovalSystem())
            if (System.getenv("debug") == "true") {
                add(DebugRenderSystem())
            }
        }

        onRemoveEntity { entity ->
            activationSystem.onRemoveEntity(entity)
        }
    }

    override fun show() {
        // UI
        setupUI()
        // input
        Gdx.input.inputProcessor = InputMultiplexer(stage, world.system<ControllerSystem>())
        // tile map initialisation
        registerTiledListeners()
        tiledService.setMap(MapAsset.TUTORIAL)
        // fade in effect
        world.system<ControllerSystem>().enabled = false
        val transitionEntity = world.entity {
            it += Transition(
                effects = gdxArrayOf(TransitionEffect(TransitionType.PIXELIZE, duration = 1.25f, reversed = true))
            )
        }
        world.entity {
            it += DelayAction(delay = 1.5f) {
                world.system<ControllerSystem>().enabled = true
                transitionEntity.remove()
            }
        }
    }

    private fun setupUI() {
        stage.addActor(GameView(gameViewModel, skin))
    }

    override fun hide() {
        tiledService.clearAllListener()
        world.removeAll(clearRecycled = true)
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