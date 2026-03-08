package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Camera shake effect with [max] offset in world units and [duration] in seconds. Tracks [currentDuration].
 */
data class CameraShake(
    val max: Float,
    val duration: Float,
) : Component<CameraShake> {
    var currentDuration = 0f

    override fun type() = CameraShake

    companion object : ComponentType<CameraShake>()
}