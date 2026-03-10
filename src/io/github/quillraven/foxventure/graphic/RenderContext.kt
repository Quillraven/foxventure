package io.github.quillraven.foxventure.graphic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport

data class RenderContext(
    val batch: Batch = SpriteBatch(),
    val gameViewport: Viewport = ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT),
    val uiViewport: Viewport = ExtendViewport(854f, 480f),
    var fbo1: FrameBuffer = frameBuffer(VIRTUAL_WIDTH * FBO_PIXELS_PER_UNIT, VIRTUAL_HEIGHT * FBO_PIXELS_PER_UNIT),
    var fbo2: FrameBuffer = frameBuffer(VIRTUAL_WIDTH * FBO_PIXELS_PER_UNIT, VIRTUAL_HEIGHT * FBO_PIXELS_PER_UNIT),
) : Disposable {
    var activeFbo: FrameBuffer = fbo1

    fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)

        if (gameViewport.worldWidth != VIRTUAL_WIDTH || gameViewport.worldHeight != VIRTUAL_HEIGHT) {
            val fboW = gameViewport.worldWidth * FBO_PIXELS_PER_UNIT
            val fboH = gameViewport.worldHeight * FBO_PIXELS_PER_UNIT
            fbo1.dispose()
            fbo1 = frameBuffer(fboW, fboH)
            fbo2.dispose()
            fbo2 = frameBuffer(fboW, fboH)
        }
        Gdx.app.debug("RenderContext", "FBO dimensions: ${fbo1.width}x${fbo1.height}")
    }

    override fun dispose() {
        batch.dispose()
        fbo1.dispose()
        fbo2.dispose()
    }

    fun swapActiveFbo() {
        activeFbo = when (activeFbo) {
            fbo1 -> fbo2
            else -> fbo1
        }
    }

    companion object {
        private const val VIRTUAL_WIDTH = 16f
        private const val VIRTUAL_HEIGHT = 9f
        private const val FBO_PIXELS_PER_UNIT = 48

        private fun frameBuffer(width: Float, height: Float) =
            FrameBuffer(Pixmap.Format.RGBA8888, width.toInt(), height.toInt(), false).apply {
                colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            }
    }
}