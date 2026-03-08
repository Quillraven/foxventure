package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.system.TransitionType
import ktx.collections.GdxArray

/**
 * A transition effect of [type] with [duration], optionally [reversed], and initial [delay]. Tracks [timer] progress.
 */
data class TransitionEffect(
    val type: TransitionType,
    val duration: Float,
    val reversed: Boolean,
    var delay: Float = 0f,
) {
    var timer: Float = 0f
}

/**
 * Manages a sequence of transition [effects] for an entity.
 */
class Transition(val effects: GdxArray<TransitionEffect>) : Component<Transition> {

    override fun type() = Transition

    companion object : ComponentType<Transition>()
}