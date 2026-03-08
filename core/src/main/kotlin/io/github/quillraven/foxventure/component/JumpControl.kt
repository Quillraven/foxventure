package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class JumpControl : Component<JumpControl> {
    var coyoteTimer: Float = 0f
    var jumpBufferTimer: Float = 0f
    var isJumping: Boolean = false
    var isJumpCommandHeld: Boolean = false

    override fun type() = JumpControl

    companion object : ComponentType<JumpControl>()
}