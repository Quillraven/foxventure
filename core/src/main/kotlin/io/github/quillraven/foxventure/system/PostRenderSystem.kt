package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.RenderContext
import ktx.graphics.use

class PostRenderSystem(
    private val renderContext: RenderContext = inject(),
    private val batch: Batch = renderContext.batch, // do not inject the FBO because it gets disposed during resize
) : IntervalSystem() {
    override fun onTick() {
        batch.use(batch.projectionMatrix.idt()) {
            batch.draw(renderContext.fbo.colorBufferTexture, -1f, 1f, 2f, -2f)
        }
    }
}