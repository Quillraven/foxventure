package io.github.quillraven.foxventure.trigger.firstboss

import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.screen.VictoryScreen
import io.github.quillraven.foxventure.trigger.trigger

fun firstBossDeathTrigger(world: World) = trigger(world) {
    action {
        onStart = {
            game.setScreen<VictoryScreen>()
        }
    }
}