package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.app.gdxError

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

enum class AnimationType {
    IDLE, RUN, JUMP, FALL, CLIMB, ATTACK, HURT;

    val atlasKey: String = name.lowercase()

    companion object {
        fun byAtlasKey(key: String): AnimationType {
            return entries.find { it.atlasKey == key } ?: gdxError("No animation type for key $key")
        }
    }
}

class Animation(
    val objectKey: String, // used inside the AnimationSystem to cache dimension updates
    val idle: GdxAnimation,
    val gdxAnimations: Map<AnimationType, GdxAnimation>,
    var speed: Float,
    var stateTime: Float = 0f,
) : Component<Animation> {

    private val defaultSpeed: Float = speed

    var active: GdxAnimation = idle
        private set

    var activeType: AnimationType = AnimationType.IDLE
        private set

    private var updateDimensions: Boolean = false

    fun changeTo(type: AnimationType) {
        val newAnimation = gdxAnimations[type]
        if (newAnimation == null) {
            active = idle
            activeType = AnimationType.IDLE
        } else {
            active = newAnimation
            activeType = type
        }
        updateDimensions = true
        stateTime = 0f
    }

    fun getAndClearUpdateDimensionsFlag() = updateDimensions.also { updateDimensions = false }

    fun isFinished(): Boolean = active.isAnimationFinished(stateTime)

    fun resetSpeed() {
        speed = defaultSpeed
    }

    override fun type() = Animation

    companion object : ComponentType<Animation>()
}
