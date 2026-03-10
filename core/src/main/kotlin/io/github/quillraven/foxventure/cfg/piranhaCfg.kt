package io.github.quillraven.foxventure.cfg

import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.PiranhaStateIdle
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.ProximityDetector
import ktx.tiled.property

fun EntityCreateContext.piranhaCfg(
    world: World,
    tile: TiledMapTile,
    entity: Entity,
) {
    entity += Fsm(FleksStateMachine(world, entity, PiranhaStateIdle))
    entity += ProximityDetector(
        range = tile.property("proximity_range"),
        predicate = { target -> target has Player }
    )
}