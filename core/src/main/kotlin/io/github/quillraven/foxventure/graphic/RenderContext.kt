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
    val gameViewport: Viewport = ExtendViewport(16f, 9f),
    val uiViewport: Viewport = ExtendViewport(854f, 480f),
    var fbo1: FrameBuffer = frameBuffer(Gdx.graphics.width, Gdx.graphics.height),
    var fbo2: FrameBuffer = frameBuffer(Gdx.graphics.width, Gdx.graphics.height),
) : Disposable {
    var activeFbo: FrameBuffer = fbo1

    fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, true)

        if (width > 0 && height > 0 && (width != fbo1.width || height != fbo1.height)) {
            fbo1.dispose()
            fbo1 = frameBuffer(width, height)
            fbo2.dispose()
            fbo2 = frameBuffer(width, height)
        }
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
        private fun frameBuffer(width: Int, height: Int) =
            FrameBuffer(Pixmap.Format.RGBA8888, width, height, false).apply {
                colorBufferTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            }
    }
}