package io.github.quillraven.foxventure.cfg

import com.badlogic.gdx.maps.objects.PointMapObject
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.FrogBossStateIdle
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.MoveToPoint
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.tiled.TiledService
import ktx.app.gdxError
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.math.vec2
import ktx.tiled.x
import ktx.tiled.y

private const val JUMP_HEIGHT = 3f
private val PHASE1_2_WAYPOINTS = listOf("left", "platform-left", "center-left", "center-right", "platform-right", "right")
private val PHASE3_WAYPOINTS = listOf("left", "center-left", "center", "center-right", "right")

fun EntityCreateContext.frogBossCfg(
    world: World,
    entity: Entity,
) {
    val tiledService = world.inject<TiledService>()
    val waypoints = bossWaypoints(tiledService)
    val collBox = entity[Collision].box
    val spawnY = entity[Transform].position.y

    // pre-compute jump sequences: index = phase * 2 + (if rtl 0 else 1)
    val jumpSequences = Array(3) { phase ->
        Array(2) { dirIdx ->
            val rtl = dirIdx == 0
            buildJumpPoints(waypoints, collBox, spawnY, phase, rtl)
        }
    }

    entity += EntityTag.BOSS
    entity += Fsm(
        FleksStateMachine(world, entity, FrogBossStateIdle),
        customProperties = mutableMapOf(
            "jump_sequences" to jumpSequences,
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

private fun buildJumpPoints(
    waypoints: Map<String, Vector2>,
    collBox: io.github.quillraven.foxventure.component.Rect,
    spawnY: Float,
    phase: Int,
    rtl: Boolean,
): GdxArray<MoveToPoint> {
    val list = if (phase < 2) PHASE1_2_WAYPOINTS else PHASE3_WAYPOINTS
    val names = if (rtl) list.dropLast(1).asReversed() else list.drop(1)
    val jumpDurations = floatArrayOf(1.5f, 1.2f, 0.9f)
    val halfDuration = jumpDurations[phase] * 0.5f

    val landingYs = names.map { name ->
        val pt = waypoints[name] ?: gdxError("No waypoint '$name'")
        pt.y - collBox.y
    }

    val points = gdxArrayOf<MoveToPoint>()
    names.forEachIndexed { i, name ->
        val pt = waypoints[name] ?: return@forEachIndexed
        val landX = pt.x - collBox.x - collBox.width * 0.5f
        val landY = landingYs[i]
        val startY = if (i == 0) spawnY else landingYs[i - 1]
        val peakY = maxOf(startY, landY) + JUMP_HEIGHT
        points.add(MoveToPoint(vec2(landX, peakY), Interpolation.smooth, halfDuration, Interpolation.pow3Out))
        points.add(MoveToPoint(vec2(landX, landY), Interpolation.smooth, halfDuration, Interpolation.pow3In))
    }
    return points
}

private fun bossWaypoints(tiledService: TiledService): Map<String, Vector2> =
    tiledService.currentMap.layers.get("boss")?.objects
        ?.filterIsInstance<PointMapObject>()
        ?.associate { it.name to vec2(it.x.toWorldUnits(), it.y.toWorldUnits()) }
        ?: gdxError("No waypoints found in boss layer")

