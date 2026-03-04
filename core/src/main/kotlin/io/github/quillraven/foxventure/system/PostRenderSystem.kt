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
    private val grayScaleUlDesaturation = grayScaleShader.getUniformLocation("u_desaturation")

    private fun shader(vertexName: String = "default.vert", fragmentName: String) =
        ShaderProgram(
            "shader/$vertexName".toInternalFile(),
            "shader/$fragmentName".toInternalFile()
        )

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
            applyShader(transition.effects.first())
            return
        }

        // multiple effects -> render to different FBOs via Ping-Pong approach
        transition.effects.forEach { effect ->
            // render to active FBO
            applyShader(effect)
            val prevActiveFbo = renderContext.activeFbo
            renderContext.swapActiveFbo()
            renderContext.activeFbo.use {
                batch.use(batch.projectionMatrix.idt()) {
                    batch.draw(prevActiveFbo.colorBufferTexture, -1f, 1f, 2f, -2f)
                }
            }
        }
    }

    private fun applyShader(effect: TransitionEffect) {
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
                batch.shader = pixelShader
                pixelShader.use {
                    val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                    pixelShader.setUniformf(pixelUlProgress, progress)
                    pixelShader.setUniformf(pixelUlRatio, gameViewport.worldWidth / gameViewport.worldHeight)
                    val minSquaresX = gameViewport.worldWidth * 6f
                    val minSquaresY = gameViewport.worldHeight * 6f
                    pixelShader.setUniformf(pixelUlSquaresMin, minSquaresX, minSquaresY)
                    pixelShader.setUniformi(pixelUlSteps, 50)
                }
            }

            TransitionType.GRAYSCALE -> {
                batch.shader = grayScaleShader
                grayScaleShader.use {
                    val progress = if (effect.reversed) (1f - easedProgress) else easedProgress
                    grayScaleShader.setUniformf(grayScaleUlDesaturation, progress)
                }
            }
        }

        // update timer
        effect.timer += deltaTime
    }

    override fun onDispose() {
        pixelShader.dispose()
        grayScaleShader.dispose()
    }
}