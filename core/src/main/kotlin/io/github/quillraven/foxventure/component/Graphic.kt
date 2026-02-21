package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import ktx.math.vec2

/**
 * Represents a graphical component tied to an entity, allowing it to display a texture
 * region on the screen. The component is responsible for tracking the graphical region and
 * its size.
 *
 * @constructor
 * Creates a new [Graphic] instance with the given texture region.
 *
 * @property region The texture region displayed by this graphical component. Updating this property
 * dynamically recalculates the size of the graphic in world units.
 * @property regionSize The size of the texture region in world units. This value is automatically
 * updated based on the [region].
 */
class Graphic(
    region: TextureRegion,
    var flip: Boolean = false,
) : Component<Graphic> {

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
