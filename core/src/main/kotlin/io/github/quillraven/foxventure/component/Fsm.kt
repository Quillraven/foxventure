package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.ai.FleksStateMachine

/**
 * Wraps a finite [state] machine for AI behavior.
 */
data class Fsm(
    val state: FleksStateMachine,
    val customProperties: Map<String, Any> = emptyMap(),
) : Component<Fsm> {
    override fun type() = Fsm

    companion object : ComponentType<Fsm>()
}