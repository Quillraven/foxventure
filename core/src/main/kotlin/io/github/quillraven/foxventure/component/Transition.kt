package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.system.TransitionType

class Transition(
    val type: TransitionType,
    val duration: Float,
    val removeAfterTransition: Boolean,
) : Component<Transition> {
    var timer: Float = 0f

    override fun type() = Transition

    companion object : ComponentType<Transition>()
}