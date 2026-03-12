package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.app.gdxError

typealias GdxAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

/**
 * Animation types mapped to their atlas keys.
 */
enum class AnimationType {
    IDLE, RUN, JUMP, FALL, CLIMB, ATTACK, HURT;

    val atlasKey: String = name.lowercase()

    companion object {
        fun byAtlasKey(key: String): AnimationType {
            return entries.find { it.atlasKey == key } ?: gdxError("No animation type for key $key")
        }
    }
}

/**
 * Manages entity animations with an [objectKey] for caching dimensions, [idle] default animation,
 * [gdxAnimations] map by type, and playback [speed]. Tracks [stateTime] and the [active] animation.
 */
class Animation(
    val objectKey: String,
    val idle: GdxAnimation,
    val gdxAnimations: Map<AnimationType, GdxAnimation>,
    var speed: Float,
) : Component<Animation> {
    var stateTime: Float = 0f

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

    fun isLastFrame(): Boolean {
        val frameIndex = active.getKeyFrameIndex(stateTime)
        return frameIndex == active.keyFrames.size - 1
    }

    fun resetSpeed() {
        speed = defaultSpeed
    }

    override fun type() = Animation

    companion object : ComponentType<Animation>() {
        fun TextureAtlas.getGdxAnimation(
            objectKey: String,
            animationType: AnimationType,
            playMode: PlayMode = PlayMode.LOOP,
        ): GdxAnimation {
            val animationKey = "$objectKey/${animationType.atlasKey}"
            val regions = this.findRegions(animationKey)
            if (regions.isEmpty) {
                gdxError("No regions for animation $animationKey")
            }
            return GdxAnimation(1 / 12f, regions, playMode)
        }
    }
}
