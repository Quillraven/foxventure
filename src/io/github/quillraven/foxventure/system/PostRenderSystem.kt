package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import io.github.quillraven.foxventure.graphic.RenderContext
import io.github.quillraven.foxventure.graphic.ShaderService
import ktx.graphics.use
import ktx.math.vec2

/**
 * Types of screen transition effects.
 */
enum class TransitionType {
    PIXELIZE, GRAYSCALE, CIRCLE_CROP
}

/**
 * Applies post-processing shader effects and transitions to the rendered framebuffer.
 */
class PostRenderSystem(
    private val renderContext: RenderContext = inject(),
    private val batch: Batch = renderContext.batch, // do not inject the FBO because it gets disposed during resize
    private val gameViewport: Viewport = inject(),
    private val shaderService: ShaderService = inject(),
) : IteratingSystem(family { all(Transition) }) {
    private val tmpVec = vec2()

    override fun onTick() {
        if (family.isEmpty) {
            // no transition effects -> render primary FBO
            batch.shader = null
            batch.use(batch.projectionMatrix.idt()) {
                batch.draw(renderContext.fbo1.colorBufferTexture, -1f, 1f, 2f, -2f)
            }
            return
        }

        // transition entity -> render with active FBO (Ping-Pong approach for effect post-processing)
        super.onTick()
        batch.use(batch.projectionMatrix.idt()) {
            batch.draw(renderContext.activeFbo.colorBufferTexture, -1f, 1f, 2f, -2f)
        }
        renderContext.activeFbo = renderContext.fbo1
    }

    override fun onTickEntity(entity: Entity) {
        val transition = entity[Transition]

        if (transition.effects.size == 1) {
            // render directly to screen for a single effect
            renderContext.activeFbo = renderContext.fbo1
            applyShader(entity, transition.effects.first())
            return
        }

        // multiple effects -> render to different FBOs via Ping-Pong approach
        transition.effects.forEach { effect ->
            // render to active FBO
            applyShader(entity, effect)
            val prevActiveFbo = renderContext.activeFbo
            renderContext.swapActiveFbo()
            renderContext.activeFbo.use {
                batch.use(batch.projectionMatrix.idt()) {
                    batch.draw(prevActiveFbo.colorBufferTexture, -1f, 1f, 2f, -2f)
                }
            }
        }
    }

    private fun applyShader(entity: Entity, effect: TransitionEffect) {
        if (effect.delay > 0f) {
            effect.delay -= deltaTime
            batch.shader = null
            return
        }

        val normalizedTime = (effect.timer / effect.duration).coerceIn(0f, 1f)
        val easedProgress = normalizedTime * normalizedTime * (3f - 2f * normalizedTime) // smoothstep easing

        // set shader and uniforms
        when (effect.type) {
            TransitionType.PIXELIZE -> {
                val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                val aspectRatio = gameViewport.worldWidth / gameViewport.worldHeight
                val minSquaresX = gameViewport.worldWidth * 6f
                val minSquaresY = gameViewport.worldHeight * 6f
                shaderService.applyPixelShader(batch, progress, aspectRatio, minSquaresX, minSquaresY)
            }

            TransitionType.GRAYSCALE -> {
                val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                shaderService.applyGrayScaleShader(batch, progress)
            }

            TransitionType.CIRCLE_CROP -> {
                val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                val aspectRatio = gameViewport.worldWidth / gameViewport.worldHeight
                // calculate the center of the circle (default is center of screen)
                var centerX = 0.5f
                var centerY = 0.5f
                entity.getOrNull(Transform)?.let { (position, size) ->
                    tmpVec.set(position.x + size.x / 2f, position.y + size.y / 2f)
                    val screenCoords = gameViewport.project(tmpVec)
                    centerX = (screenCoords.x - gameViewport.screenX) / gameViewport.screenWidth
                    centerY = (screenCoords.y - gameViewport.screenY) / gameViewport.screenHeight
                }
                shaderService.applyCircleCropShader(batch, progress, aspectRatio, centerX, centerY, Color.BLACK)
            }
        }

        // update timer
        effect.timer += deltaTime
    }
}