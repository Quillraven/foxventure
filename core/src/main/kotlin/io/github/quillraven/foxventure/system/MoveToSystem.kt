package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.MoveTo
import io.github.quillraven.foxventure.component.Transform

/**
 * Moves entities through a sequence of interpolated waypoints.
 */
class MoveToSystem : IteratingSystem(
    family = family { all(Transform, MoveTo, EntityTag.ACTIVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val moveTo = entity[MoveTo]
        if (moveTo.pointIdx >= moveTo.points.size) {
            entity.configure { it -= MoveTo }
            return
        }

        val (position) = entity[Transform]
        if (moveTo.elapsed == 0f) {
            moveTo.startPosition.set(position)
        }

        val point = moveTo.points[moveTo.pointIdx]
        // progress (0f..1f)
        moveTo.elapsed += deltaTime
        val progress = MathUtils.clamp(moveTo.elapsed / point.duration, 0f, 1f)
        // update position
        position.x = point.interpolation.apply(moveTo.startPosition.x, point.target.x, progress)
        position.y = (point.interpolationY ?: point.interpolation).apply(moveTo.startPosition.y, point.target.y, progress)
        // flip graphic
        if (!MathUtils.isEqual(moveTo.startPosition.x, point.target.x, 0.001f)) {
            entity.getOrNull(Graphic)?.let { graphic ->
                graphic.flip = moveTo.startPosition.x > point.target.x
            }
        }

        if (progress >= 1f) {
            moveTo.pointIdx++
            moveTo.elapsed = 0f
        }
    }
}
