package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.collections.GdxMap

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

enum class AnimationType {
    IDLE, RUN, JUMP, FALL, CLIMB
}

class Animation(
    val idle: GdxAnimation,
    val gdxAnimations: GdxMap<AnimationType, GdxAnimation>,
    var stateTime: Float = 0f,
    var speed: Float = 1f,
) : Component<Animation> {

    var active: GdxAnimation = idle
        private set

    fun changeTo(type: AnimationType) {
        active = gdxAnimations[type] ?: idle
        stateTime = 0f
    }

    override fun type() = Animation

    companion object : ComponentType<Animation>()
}
