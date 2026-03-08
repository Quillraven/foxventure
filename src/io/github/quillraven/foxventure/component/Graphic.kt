package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import ktx.math.vec2

/**
 * Displays a texture [region] with [regionSize] in world units, optional [flip], [color] tint, and render [offset].
 */
class Graphic(
    region: TextureRegion,
    var flip: Boolean = false,
    val color: Color = Color.WHITE.cpy(),
) : Component<Graphic> {
    var offset: Vector2 = vec2(0f, 0f)

    val regionSize: Vector2 = vec2(region.regionWidth.toWorldUnits(), region.regionHeight.toWorldUnits())

    var region: TextureRegion = region
        set(value) {
            regionSize.x = value.regionWidth.toWorldUnits()
            regionSize.y = value.regionHeight.toWorldUnits()
            field = value
        }

    operator fun component1(): TextureRegion = region

    operator fun component2(): Vector2 = regionSize

    operator fun component3(): Boolean = flip

    override fun type() = Graphic

    companion object : ComponentType<Graphic>()
}
