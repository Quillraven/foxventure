package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

data class ProximityDetector(
    var squaredRange: Float,
    var predicate: (World.(target: Entity) -> Boolean),
    var onDetect: (World.(source: Entity, target: Entity) -> Unit),
    var onBreak: (World.(source: Entity, target: Entity) -> Unit),
    var target: Entity = Entity.NONE,
) : Component<ProximityDetector> {
    override fun type() = ProximityDetector

    companion object : ComponentType<ProximityDetector>()
}
