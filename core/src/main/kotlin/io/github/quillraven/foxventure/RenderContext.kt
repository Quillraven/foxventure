package io.github.quillraven.foxventure

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.Disposable

data class RenderContext(
    val batch: Batch = SpriteBatch(),
    var fbo: FrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, false)
) : Disposable {
    fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0 && (width != fbo.width || height != fbo.height)) {
            fbo.dispose()
            fbo = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        }
    }

    override fun dispose() {
        batch.dispose()
        fbo.dispose()
    }
}