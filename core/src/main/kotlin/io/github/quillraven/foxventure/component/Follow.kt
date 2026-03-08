package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class Follow(
    distance: Float,
    breakDistance: Float,
    val stopAtCliff: Boolean,
) : Component<Follow> {
    var squaredDistance: Float = distance * distance
    var squaredBreakDistance: Float = breakDistance * breakDistance
    var target: Entity = Entity.NONE
    var moveDirection: Float = 0f

    override fun type() = Follow

    companion object : ComponentType<Follow>()
}
