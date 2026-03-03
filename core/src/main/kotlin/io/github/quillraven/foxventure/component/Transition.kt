package io.github.quillraven.foxventure.component

import com.badlogic.gdx.utils.Timer
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.system.TransitionType

class Transition(
    val type: TransitionType,
    val duration: Float,
    val actionDelay: Float = 0f,
    action: () -> Unit
) : Component<Transition> {
    var timer: Float = 0f

    val task: Timer.Task = object : Timer.Task() {
        override fun run() = action()
    }

    override fun type() = Transition

    companion object : ComponentType<Transition>()
}