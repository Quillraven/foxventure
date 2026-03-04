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
import ktx.assets.toInternalFile
import ktx.graphics.use

enum class TransitionType {
    NONE, PIXELIZE_OUT, PIXELIZE_IN, GRAYSCALE_OUT
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
        super.onTick()

        batch.use(batch.projectionMatrix.idt()) {
            batch.draw(renderContext.fbo.colorBufferTexture, -1f, 1f, 2f, -2f)
        }
    }

    override fun onTickEntity(entity: Entity) {
        val transition = entity[Transition]

        if (transition.timer >= transition.duration) {
            // transition done
            if (transition.removeAfterTransition) {
                entity.remove()
            } else {
                entity.configure { it -= Transition }
            }
            return
        }

        val normalizedTime = (transition.timer / transition.duration).coerceIn(0f, 1f)
        val easedProgress = normalizedTime * normalizedTime * (3f - 2f * normalizedTime) // smoothstep easing

        // set shader and uniforms
        val shader = when (transition.type) {
            TransitionType.NONE -> null
            TransitionType.PIXELIZE_OUT, TransitionType.PIXELIZE_IN -> pixelShader.also {
                val isInTransition = transition.type == TransitionType.PIXELIZE_IN // use reversed progress
                val progress = if (isInTransition) (1f - easedProgress) else easedProgress
                it.setUniformf(pixelUlProgress, progress)
                it.setUniformf(pixelUlRatio, gameViewport.worldWidth / gameViewport.worldHeight)
                it.setUniformf(pixelUlSquaresMin, gameViewport.worldWidth * 6, gameViewport.worldHeight * 6)
                it.setUniformi(pixelUlSteps, 50)
            }

            TransitionType.GRAYSCALE_OUT -> grayScaleShader.also {
                it.setUniformf(grayScaleUniformDesaturation, easedProgress)
            }
        }
        if (batch.shader != shader) {
            batch.shader = shader
        }

        // update timer
        transition.timer += deltaTime
    }

    override fun onDispose() {
        pixelShader.dispose()
        grayScaleShader.dispose()
    }
}