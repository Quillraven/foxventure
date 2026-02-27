package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class Follow(
    var squaredDistance: Float,
    var squaredBreakDistance: Float,
    val stopAtCliff: Boolean,
    var target: Entity = Entity.NONE,
    var moveDirection: Float = 0f,
) : Component<Follow> {
    override fun type() = Follow

    companion object : ComponentType<Follow>()
}
