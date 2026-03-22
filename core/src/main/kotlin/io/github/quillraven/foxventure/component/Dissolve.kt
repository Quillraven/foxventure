package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * Drives the dissolve shader for an entity. UV data is read from the TextureRegion at add-time.
 */
data class Dissolve(
    val duration: Float,
    val uvOffsetU: Float,
    val uvOffsetV: Float,
    val atlasMaxU: Float,
    val atlasMaxV: Float,
    var timer: Float = 0f,
) : Component<Dissolve> {
    val progress: Float get() = (timer / duration).coerceIn(0f, 1f)

    override fun type() = Dissolve

    companion object : ComponentType<Dissolve>()
}
