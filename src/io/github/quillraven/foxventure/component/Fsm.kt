package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.ai.FleksStateMachine

data class Fsm(
    val state: FleksStateMachine,
) : Component<Fsm> {
    override fun type() = Fsm

    companion object : ComponentType<Fsm>()
}