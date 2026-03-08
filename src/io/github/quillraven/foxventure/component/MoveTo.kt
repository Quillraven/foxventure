package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.collections.GdxArray
import ktx.math.vec2

/**
 * A movement point with [target] position, [interpolation] curve, [duration], and optional separate [interpolationY].
 */
data class MoveToPoint(
    val target: Vector2,
    val interpolation: Interpolation,
    val duration: Float,
    val interpolationY: Interpolation? = null,
)

/**
 * Moves an entity through a sequence of [points]. Tracks [startPosition], [elapsed] time, and current [pointIdx].
 */
data class MoveTo(
    val points: GdxArray<MoveToPoint>,
) : Component<MoveTo> {
    val startPosition: Vector2 = vec2()
    var elapsed: Float = 0f
    var pointIdx: Int = 0

    override fun type() = MoveTo

    companion object : ComponentType<MoveTo>()
}
