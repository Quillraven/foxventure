package io.github.quillraven.foxventure.cfg

import com.badlogic.gdx.maps.objects.PointMapObject
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.PiranhaStateIdle
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.ProximityDetector
import ktx.math.vec2
import ktx.tiled.property
import ktx.tiled.x
import ktx.tiled.y

fun EntityCreateContext.piranhaCfg(
    world: World,
    tile: TiledMapTile,
    entity: Entity,
) {
    val spawnPoint = tile.objects.single { it is PointMapObject } as PointMapObject
    val collBox = entity[Collision].box
    val offsetX = spawnPoint.x.toWorldUnits() - collBox.x
    val offsetY = spawnPoint.y.toWorldUnits() - collBox.y

    entity += Fsm(
        FleksStateMachine(world, entity, PiranhaStateIdle),
        customProperties = mapOf(
            "projectile_speed" to tile.property<Float>("projectile_speed"),
            "projectile_spawn_offset" to vec2(offsetX, offsetY),
        ),
    )
    entity += ProximityDetector(
        range = tile.property("proximity_range"),
        predicate = { target -> target has Player }
    )
}