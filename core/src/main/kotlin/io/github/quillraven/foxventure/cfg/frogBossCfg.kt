package io.github.quillraven.foxventure.cfg

import com.badlogic.gdx.maps.objects.PointMapObject
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.FrogBossStateIdle
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.tiled.TiledService
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.x
import ktx.tiled.y

fun EntityCreateContext.frogBossCfg(
    world: World,
    entity: Entity,
) {
    val tiledService = world.inject<TiledService>()
    entity += EntityTag.BOSS
    entity += Fsm(
        FleksStateMachine(world, entity, FrogBossStateIdle),
        customProperties = mutableMapOf(
            "waypoints" to bossWaypoints(tiledService),
            "jump_duration" to floatArrayOf(1.5f, 1.2f, 0.9f),
            "vulnerable_duration" to floatArrayOf(2.5f, 1.8f, 1.2f),
            "direction" to -1,
            "platforms_destroyed" to false,
            "stun_entity" to Entity.NONE,
            "life_on_enter" to 0f,
            "phase" to 0,
        ),
    )
}

private fun bossWaypoints(tiledService: TiledService): Map<String, Vector2> =
    tiledService.currentMap.layers.get("boss")?.objects
        ?.filterIsInstance<PointMapObject>()
        ?.associate { it.name to vec2(it.x.toWorldUnits(), it.y.toWorldUnits()) }
        ?: gdxError("No waypoints found in boss layer")
