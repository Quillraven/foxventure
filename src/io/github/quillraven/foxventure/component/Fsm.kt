package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.ai.FleksStateMachine

/**
 * Wraps a finite [state] machine for AI behavior.
 */
data class Fsm(
    val state: FleksStateMachine,
    val customProperties: MutableMap<String, Any> = mutableMapOf(),
) : Component<Fsm> {
    inline fun <reified T> customProperty(key: String): T = customProperties[key] as T

    override fun type() = Fsm

    companion object : ComponentType<Fsm>()
}