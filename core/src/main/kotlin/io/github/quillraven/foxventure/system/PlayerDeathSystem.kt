package io.github.quillraven.foxventure.system

import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.GdxGame
import io.github.quillraven.foxventure.ai.PlayerStateDeath
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.DelayAction
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import io.github.quillraven.foxventure.screen.GameScreen
import ktx.collections.gdxArrayOf
import kotlin.math.max

class PlayerDeathSystem(
    private val gameViewport: Viewport = inject(),
    private val game: GdxGame = inject(),
) : IteratingSystem(
    family = family { all(Transform, EntityTag.PLAYER_DEATH) }
) {
    private var deathTime = 0f
    private var velocityY = 0f

    override fun onTickEntity(entity: Entity) {
        if (deathTime == 0f) {
            onPlayerDeath(entity)
            return
        }

        deathTime += deltaTime
        if (deathTime < MOVE_DELAY) {
            // keep the player in position for a short period
            return
        }

        velocityY -= 20f * deltaTime
        val (position, size) = entity[Transform]
        position.y += velocityY * deltaTime

        val cameraBottom = gameViewport.camera.position.y - gameViewport.worldHeight / 2
        val playerTop = position.y + size.y
        if (playerTop < cameraBottom) {
            deathTime = 0f
            velocityY = 0f
            entity.configure { it -= EntityTag.PLAYER_DEATH }
        }
    }

    private fun onPlayerDeath(entity: Entity) {
        entity.configure {
            it -= Collision
            it -= Physics
            it -= Controller
        }
        entity[Fsm].state.changeState(PlayerStateDeath)
        velocityY = 8f

        world.system<ActivationSystem>().enabled = false
        world.system<AerialMoveSystem>().enabled = false
        world.system<AttackSystem>().enabled = false
        world.system<CameraSystem>().enabled = false
        world.system<ClimbSystem>().enabled = false
        world.system<CollisionSystem>().enabled = false
        world.system<DamagedSystem>().enabled = false
        world.system<DamageRequestSystem>().enabled = false
        world.system<DelayRemovalSystem>().enabled = false
        world.system<FlashSystem>().enabled = false
        world.system<FollowSystem>().enabled = false
        world.system<GroundMoveSystem>().enabled = false
        world.system<LifeSystem>().enabled = false
        world.system<ProximityDetectorSystem>().enabled = false
        world.system<WanderSystem>().enabled = false
        deathTime = max(0.01f, deltaTime)

        val transitionEntity = world.entity {
            it += Transition(
                effects = gdxArrayOf(
                    TransitionEffect(TransitionType.GRAYSCALE, duration = 1f, reversed = false, delay = 0f),
                    TransitionEffect(TransitionType.PIXELIZE, duration = 2f, reversed = false, delay = 1f),
                )
            )
        }
        world.entity {
            it += DelayAction(delay = 4f) {
                transitionEntity.remove()
                game.getScreen<GameScreen>().dispose()
                game.removeScreen<GameScreen>()
                game.addScreen(GameScreen(game))
                game.setScreen<GameScreen>()
            }
        }
    }

    companion object {
        private const val MOVE_DELAY = 0.5f
    }
}
