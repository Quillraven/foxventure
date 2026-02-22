package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class JumpControl(
    var coyoteTimer: Float = 0f,
    var jumpBufferTimer: Float = 0f,
    var jumpInput: Boolean = false,
    var wasJumpPressed: Boolean = false,
) : Component<JumpControl> {
    override fun type() = JumpControl

    companion object : ComponentType<JumpControl>()
}