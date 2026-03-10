package io.github.quillraven.foxventure.cfg

import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.MushroomStateIdle
import io.github.quillraven.foxventure.component.Follow
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.ProximityDetector
import ktx.tiled.property

fun EntityCreateContext.mushroomCfg(
    world: World,
    tile: TiledMapTile,
    entity: Entity,
) {
    entity += Fsm(FleksStateMachine(world, entity, MushroomStateIdle))
    entity += ProximityDetector(
        range = tile.property("proximity_range"),
        predicate = { target -> target has Player },
        onDetect = { source, target -> source[Follow].target = target },
        onBreak = { source, _ -> source[Follow].target = Entity.NONE }
    )

    val followProps = tile.property<MapProperties>("follow")
    entity += Follow(
        distance = followProps["range"] as Float,
        breakDistance = followProps["break_range"] as Float,
        stopAtCliff = followProps["stop_at_cliff"] as Boolean,
    )
}