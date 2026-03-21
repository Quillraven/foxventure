package io.github.quillraven.foxventure.ai

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType

data object FrogBossStateIdle : FsmState {
    override fun World.onEnter(entity: Entity) {
        entity[Animation].run {
            changeTo(AnimationType.IDLE)
            speed = 0f
        }
    }
}
