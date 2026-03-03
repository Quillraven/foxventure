package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Timer
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
    NONE, PIXELIZE_GRAYSCALE_OUT, PIXELIZE_IN
}

class PostRenderSystem(
    private val renderContext: RenderContext = inject(),
    private val batch: Batch = renderContext.batch, // do not inject the FBO because it gets disposed during resize
    private val gameViewport: Viewport = inject(),
) : IteratingSystem(family { all(Transition) }) {
    private val pixelShader = shader(fragmentName = "pixelize.frag")
    private val pixelUniformAmount = pixelShader.getUniformLocation("u_amount")
    private val pixelUniformProgress = pixelShader.getUniformLocation("u_progress")
    private val pixelUniformDesaturation = pixelShader.getUniformLocation("u_desaturation")

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
            Timer.schedule(transition.task, transition.actionDelay)
            entity.remove()
            return
        }

        val normalizedTime = (transition.timer / transition.duration).coerceIn(0f, 1f)
        val easedProgress = normalizedTime * normalizedTime * (3f - 2f * normalizedTime) // smoothstep easing

        // set shader and uniforms
        val shader = when (transition.type) {
            TransitionType.NONE -> null
            TransitionType.PIXELIZE_GRAYSCALE_OUT -> pixelShader.also {
                it.setUniformf(pixelUniformAmount, gameViewport.worldWidth * 7, gameViewport.worldHeight * 7)
                it.setUniformf(pixelUniformProgress, easedProgress)
                it.setUniformf(pixelUniformDesaturation, easedProgress)
            }

            TransitionType.PIXELIZE_IN -> pixelShader.also {
                val inverseProgress = 1f - easedProgress
                it.setUniformf(pixelUniformAmount, gameViewport.worldWidth * 7, gameViewport.worldHeight * 7)
                it.setUniformf(pixelUniformProgress, inverseProgress)
                it.setUniformf(pixelUniformDesaturation, 0f)
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
    }
}