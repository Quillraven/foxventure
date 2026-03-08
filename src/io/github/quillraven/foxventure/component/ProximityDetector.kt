package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

/**
 * Detects entities within [range] using a [predicate], invoking [onDetect] when found
 * and [onBreak] when lost. Tracks the current [target].
 */
class ProximityDetector(
    range: Float,
    var predicate: (World.(target: Entity) -> Boolean),
    var onDetect: (World.(source: Entity, target: Entity) -> Unit) = { _, _ -> },
    var onBreak: (World.(source: Entity, target: Entity) -> Unit) = { _, _ -> },
) : Component<ProximityDetector> {
    var squaredRange: Float = range * range
    var target: Entity = Entity.NONE

    operator fun component1() = squaredRange

    operator fun component2() = predicate

    operator fun component3() = onDetect

    operator fun component4() = onBreak

    operator fun component5() = target

    override fun type() = ProximityDetector

    companion object : ComponentType<ProximityDetector>()
}
