package io.github.quillraven.foxventure.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.foxventure.AudioService
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.PhysicsTimer
import io.github.quillraven.foxventure.component.DelayAction
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import io.github.quillraven.foxventure.graphic.RenderContext
import io.github.quillraven.foxventure.graphic.ShaderService
import io.github.quillraven.foxventure.system.ActivationSystem
import io.github.quillraven.foxventure.system.AerialMoveSystem
import io.github.quillraven.foxventure.system.AnimationSystem
import io.github.quillraven.foxventure.system.AttackSystem
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
import io.github.quillraven.foxventure.system.FsmSystem
import io.github.quillraven.foxventure.system.GroundMoveSystem
import io.github.quillraven.foxventure.system.InvulnerabilitySystem
import io.github.quillraven.foxventure.system.ItemSystem
import io.github.quillraven.foxventure.system.LifeSystem
import io.github.quillraven.foxventure.system.MoveToSystem
import io.github.quillraven.foxventure.system.PlayerDeathSystem
import io.github.quillraven.foxventure.system.PlayerVictorySystem
import io.github.quillraven.foxventure.system.PostInterpolationSystem
import io.github.quillraven.foxventure.system.PostRenderSystem
import io.github.quillraven.foxventure.system.PreInterpolationSystem
import io.github.quillraven.foxventure.system.ProjectileRemovalSystem
import io.github.quillraven.foxventure.system.ProjectileRequestSystem
import io.github.quillraven.foxventure.system.ProximityDetectorSystem
import io.github.quillraven.foxventure.system.RenderSystem
import io.github.quillraven.foxventure.system.SpawnSystem
import io.github.quillraven.foxventure.system.StunSystem
import io.github.quillraven.foxventure.system.TransitionType
import io.github.quillraven.foxventure.system.TriggerSystem
import io.github.quillraven.foxventure.system.UiRenderSystem
import io.github.quillraven.foxventure.system.WanderSystem
import io.github.quillraven.foxventure.tiled.LoadTileObjectListener
import io.github.quillraven.foxventure.tiled.LoadTriggerListener
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.tiled.TiledService
import io.github.quillraven.foxventure.ui.GameView
import io.github.quillraven.foxventure.ui.GameViewModel
import io.github.quillraven.foxventure.ui.ShopViewModel
import io.github.quillraven.foxventure.ui.widget.ShopView
import ktx.app.KtxScreen
import ktx.collections.gdxArrayOf

class GameScreen(
    private val game: GdxGame,
    private val renderContext: RenderContext = game.serviceLocator.renderContext,
    private val stage: Stage = game.stage,
    private val tiledService: TiledService = game.serviceLocator.tiledService,
    private val audioService: AudioService = game.serviceLocator.audioService,
    private val shaderService: ShaderService = game.serviceLocator.shaderService,
    private val skin: Skin = game.skin,
) : KtxScreen {
    private val objectsAtlas = TextureAtlas(game.serviceLocator.fileHandleResolver.resolve("graphics/objects.atlas"))
    private val physicsTimer = PhysicsTimer(interval = 1 / 60f)
    private val gameViewModel = GameViewModel()
    private val shopViewModel = ShopViewModel()
    private val world: World = ecsWorld()

    private fun ecsWorld() = configureWorld {
        injectables {
            add(renderContext)
            add(renderContext.gameViewport)
            add(stage)
            add(objectsAtlas)
            add(tiledService)
            add(physicsTimer)
            add(audioService)
            add(game)
            add(shaderService)
            add(gameViewModel)
            add(shopViewModel)
            add(skin)
        }

        val activationSystem = ActivationSystem(renderContext.gameViewport)
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
            add(PlayerVictorySystem())
            add(DamagedSystem())
            add(InvulnerabilitySystem())
            add(StunSystem())
            add(LifeSystem())
            add(ItemSystem())
            add(PlayerDeathSystem())
            add(FsmSystem())
            add(DamageRequestSystem())
            add(ProjectileRequestSystem())
            add(CameraSystem())
            add(AnimationSystem())
            add(FlashSystem())
            add(PostInterpolationSystem()) // run it after all physics systems run
            add(RenderSystem())
            add(PostRenderSystem())
            add(UiRenderSystem())
            if (System.getenv("debug") == "true") {
                add(DebugRenderSystem())
            }
            add(DelayActionSystem())
            add(DelayRemovalSystem())
            add(ProjectileRemovalSystem())
            add(TriggerSystem())
        }

        onRemoveEntity { entity ->
            activationSystem.onRemoveEntity(entity)
        }
    }

    fun setMap(mapName: String) {
        tiledService.setMap(mapName)
        // fade in effect
        val controllerSystem = world.system<ControllerSystem>()
        controllerSystem.enabled = false
        val transitionEntity = world.entity {
            val player = world.family { all(Player) }.single()
            it += Transition(
                effects = gdxArrayOf(TransitionEffect(TransitionType.CIRCLE_CROP, duration = 2.5f, reversed = false))
            )
            // transform to set circle crop shader circle center position
            it += player[Transform]
        }
        world.entity {
            it += DelayAction(delay = 1.5f) {
                controllerSystem.enabled = true
                transitionEntity.remove()
            }
        }
    }

    override fun show() {
        // UI
        setupUI()
        // input
        Gdx.input.inputProcessor = InputMultiplexer(stage, world.system<ControllerSystem>())
        // tile map initialisation
        registerTiledListeners()
    }

    private fun setupUI() {
        stage.clear()
        // game hud
        stage.addActor(GameView(gameViewModel, skin))

        // shop
        shopViewModel.world = world
        val shopView = ShopView(shopViewModel, skin)
        stage.addActor(shopView)
        shopView.isVisible = false
    }

    override fun hide() {
        tiledService.clearAllListener()
        world.removeAll(clearRecycled = true)
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            gamePaused = !gamePaused
            when (gamePaused) {
                true -> audioService.pause()
                false -> audioService.resume()
            }
        }

        if (gamePaused) {
            physicsTimer.update(0f)
            world.update(0f)
            return
        }

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
            if (system is LoadTriggerListener) {
                tiledService.addLoadTriggerListener(system)
            }
        }
        tiledService.addMapChangeListener(audioService)
    }

    override fun dispose() {
        world.dispose()
        objectsAtlas.dispose()
    }

    companion object {
        var playerCredits = 0
        var playerGems = 0
        var playerLife = 0f
        var playerLifeMax = 0
        var gamePaused = false

        fun resetPlayerStats() {
            playerCredits = 4
            playerGems = 0
            playerLife = 4f
            playerLifeMax = 4
        }
    }
}
