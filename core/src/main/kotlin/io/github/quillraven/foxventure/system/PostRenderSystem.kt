package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.RenderContext
import io.github.quillraven.foxventure.component.Transition
import io.github.quillraven.foxventure.component.TransitionEffect
import ktx.assets.toInternalFile
import ktx.graphics.use

enum class TransitionType {
    PIXELIZE, GRAYSCALE
}

class PostRenderSystem(
    private val renderContext: RenderContext = inject(),
    private val batch: Batch = renderContext.batch, // do not inject the FBO because it gets disposed during resize
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(family { all(Transition) }) {
    private val pixelShader = shader(fragmentName = "pixelize.frag")
    private val pixelUlProgress = pixelShader.getUniformLocation("u_progress")
    private val pixelUlRatio = pixelShader.getUniformLocation("u_ratio")
    private val pixelUlSquaresMin = pixelShader.getUniformLocation("u_squares_min")
    private val pixelUlSteps = pixelShader.getUniformLocation("u_steps")

    private val grayScaleShader = shader(fragmentName = "grayscale.frag")
    private val grayScaleUniformDesaturation = grayScaleShader.getUniformLocation("u_desaturation")

    private fun shader(vertexName: String = "default.vert", fragmentName: String) =
        ShaderProgram(
            "shader/$vertexName".toInternalFile(),
            "shader/$fragmentName".toInternalFile()
        )

    override fun onTick() {
        if (family.isEmpty) {
            // no transition effects -> render primary FBO
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
            if (applyEffect(transition.effects.first())) {
                // transition done
                entity.remove()
            }
            return
        }

        // multiple effects -> render to different FBOs via Ping-Pong approach
        var allEffectsDone = true
        transition.effects.forEach { effect ->
            if (!applyEffect(effect)) {
                allEffectsDone = false
            }

            // render to active FBO
            val prevActiveFbo = renderContext.activeFbo
            renderContext.swapActiveFbo()
            renderContext.activeFbo.use {
                batch.use(batch.projectionMatrix.idt()) {
                    batch.draw(prevActiveFbo.colorBufferTexture, -1f, 1f, 2f, -2f)
                }
            }
        }
        if (allEffectsDone) {
            entity.remove()
        }
    }

    private fun applyEffect(effect: TransitionEffect): Boolean {
        if (effect.delay > 0f) {
            effect.delay -= deltaTime
            return false
        }

        val normalizedTime = (effect.timer / effect.duration).coerceIn(0f, 1f)
        val easedProgress = normalizedTime * normalizedTime * (3f - 2f * normalizedTime) // smoothstep easing

        // set shader and uniforms
        batch.shader = when (effect.type) {
            TransitionType.PIXELIZE -> pixelShader.also {
                val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                it.setUniformf(pixelUlProgress, progress)
                it.setUniformf(pixelUlRatio, gameViewport.worldWidth / gameViewport.worldHeight)
                it.setUniformf(pixelUlSquaresMin, gameViewport.worldWidth * 6, gameViewport.worldHeight * 6)
                it.setUniformi(pixelUlSteps, 50)
            }

            TransitionType.GRAYSCALE -> grayScaleShader.also {
                val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                it.setUniformf(grayScaleUniformDesaturation, progress)
            }
        }

        // update timer
        effect.timer += deltaTime
        return effect.timer >= effect.duration
    }

    override fun onDispose() {
        pixelShader.dispose()
        grayScaleShader.dispose()
    }
}