package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.app.gdxError

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

enum class AnimationType {
    IDLE, RUN, JUMP, FALL, CLIMB, ATTACK;

    val atlasKey: String = name.lowercase()

    companion object {
        fun byAtlasKey(key: String): AnimationType {
            return entries.find { it.atlasKey == key } ?: gdxError("No animation type for key $key")
        }
    }
}

class Animation(
    val idle: GdxAnimation,
    val gdxAnimations: Map<AnimationType, GdxAnimation>,
    var speed: Float,
    var stateTime: Float = 0f,
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
