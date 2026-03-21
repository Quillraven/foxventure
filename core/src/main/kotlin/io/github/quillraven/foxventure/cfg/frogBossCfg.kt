package io.github.quillraven.foxventure.cfg

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.FrogBossStateIdle
import io.github.quillraven.foxventure.component.Fsm

fun EntityCreateContext.frogBossCfg(
    world: World,
    entity: Entity,
) {
    entity += Fsm(
        FleksStateMachine(world, entity, FrogBossStateIdle),
    )
}