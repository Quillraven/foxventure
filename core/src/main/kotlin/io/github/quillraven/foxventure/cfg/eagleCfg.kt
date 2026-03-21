package io.github.quillraven.foxventure.cfg

import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.ai.EagleStateIdle
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.ProximityDetector
import io.github.quillraven.foxventure.component.Transform
import ktx.tiled.property

fun EntityCreateContext.eagleCfg(
    world: World,
    tile: TiledMapTile,
    entity: Entity,
) {
    entity += Fsm(
        FleksStateMachine(world, entity, EagleStateIdle),
        customProperties = mutableMapOf(
            "dive_time" to tile.property<Float>("dive_time"),
            "dive_peak_time" to tile.property<Float>("dive_peak_time"),
            "rise_time" to tile.property<Float>("rise_time"),
        ),
    )
    entity += ProximityDetector(
        range = tile.property("proximity_range"),
        predicate = { target ->
            target has Player && target[Transform].position.y < entity[Transform].position.y
        },
    )
}