package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class CameraShake(
    val max: Float, // max shake offset in world units
    val duration: Float, // duration in seconds
) : Component<CameraShake> {
    var currentDuration = 0f

    override fun type() = CameraShake

    companion object : ComponentType<CameraShake>()
}