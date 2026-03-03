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
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.screen.GameScreen

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
            deathTime = 0.01f
            return
        }

        deathTime += deltaTime
        if (deathTime < MOVE_DELAY) {
            return
        }

        velocityY -= 20f * deltaTime
        val position = entity[Transform].position
        position.y += velocityY * deltaTime

        val cameraBottom = gameViewport.camera.position.y - gameViewport.worldHeight / 2
        if (position.y + 4f < cameraBottom) {
            deathTime = 0f
            velocityY = 0f
            world.entity {
                it += Transition(type = TransitionType.PIXELIZE_OUT, duration = 2f) {
                    game.getScreen<GameScreen>().dispose()
                    game.removeScreen<GameScreen>()
                    game.addScreen(GameScreen(game))
                    game.setScreen<GameScreen>()
                }
            }
            entity.remove()
        }
    }

    companion object {
        private const val MOVE_DELAY = 0.5f
    }
}
