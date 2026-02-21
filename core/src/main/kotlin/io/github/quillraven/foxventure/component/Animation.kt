package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

class Animation(
    val idle: GdxAnimation,
    val run: GdxAnimation? = null,
    val jump: GdxAnimation? = null,
    val fall: GdxAnimation? = null,
    val climb: GdxAnimation? = null,
    var stateTime: Float = 0f,
) : Component<Animation> {

    var active: GdxAnimation = idle
        private set

    fun changeTo(animation: GdxAnimation?) {
        active = animation ?: idle
        stateTime = 0f
    }

    override fun type() = Animation

    companion object : ComponentType<Animation>()
}
