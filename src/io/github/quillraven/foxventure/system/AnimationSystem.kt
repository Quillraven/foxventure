package io.github.quillraven.foxventure.system

import com.badlogic.gdx.math.Vector3
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Transform
import ktx.collections.gdxMapOf
import ktx.collections.getOrPut
import ktx.math.component1
import ktx.math.component2
import ktx.math.component3
import ktx.math.vec3

class AnimationSystem : IteratingSystem(
    family = family { all(Animation, Graphic, EntityTag.ACTIVE) }
) {
    private val dimensionCache = gdxMapOf<String, Vector3>()

    override fun onTickEntity(entity: Entity) {
        val animation = entity[Animation]
        val graphic = entity[Graphic]
        graphic.region = animation.active.getKeyFrame(animation.stateTime)
        animation.stateTime += deltaTime * animation.speed

        if (animation.objectKey.isNotBlank() && animation.getAndClearUpdateDimensionsFlag()) {
            // adjust Transform/Graphic because the new actives frames
            // might have a different dimension than the idle frames
            val dimension = dimensionCache.getOrPut("${animation.objectKey}_${animation.activeType.atlasKey}") {
                val idleWidth = animation.idle.getKeyFrame(0f).regionWidth.toWorldUnits()
                val activeWidth = animation.active.getKeyFrame(0f).regionWidth.toWorldUnits()
                val activeHeight = animation.active.getKeyFrame(0f).regionHeight.toWorldUnits()
                vec3(activeWidth, activeHeight, activeWidth - idleWidth)
            }

            if (dimension.isZero) return

            val (width, height, widthDiff) = dimension
            entity[Transform].size.set(width, height)
            graphic.offset.x = if (graphic.flip) -widthDiff else 0f
        }
    }
}