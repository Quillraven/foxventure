package io.github.quillraven.foxventure.trigger.firstboss

import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.ai.FrogBossStateJump
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.trigger.trigger

fun IntervalSystem.firstBossStartTrigger() = trigger {
    action {
        onStart = {
            val boss = this@trigger.world.family { all(EntityTag.BOSS) }.single()
            boss[Fsm].state.changeState(FrogBossStateJump)
        }
    }
}