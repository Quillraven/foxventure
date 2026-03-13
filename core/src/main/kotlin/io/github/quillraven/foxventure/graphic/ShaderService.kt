package io.github.quillraven.foxventure.graphic

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import ktx.assets.toInternalFile
import ktx.graphics.use

class ShaderService : Disposable {
    private val pixelShader = shader(fragmentName = "pixelize.frag")
    private val pixelUlProgress = pixelShader.getUniformLocation("u_progress")
    private val pixelUlRatio = pixelShader.getUniformLocation("u_ratio")
    private val pixelUlSquaresMin = pixelShader.getUniformLocation("u_squares_min")
    private val pixelUlSteps = pixelShader.getUniformLocation("u_steps")

    private val grayScaleShader = shader(fragmentName = "grayscale.frag")
    private val grayScaleUlDesaturation = grayScaleShader.getUniformLocation("u_desaturation")

    private val circleCropShader = shader(fragmentName = "circle_crop.frag")
    private val circleCropUlProgress = circleCropShader.getUniformLocation("u_progress")
    private val circleCropUlRatio = circleCropShader.getUniformLocation("u_ratio")
    private val circleCropUlBgColor = circleCropShader.getUniformLocation("u_bgcolor")
    private val circleCropUlCenter = circleCropShader.getUniformLocation("u_center")

    private fun shader(vertexName: String = "default.vert", fragmentName: String) =
        ShaderProgram(
            "shader/$vertexName".toInternalFile(),
            "shader/$fragmentName".toInternalFile()
        )

    fun applyPixelShader(
        batch: Batch,
        progress: Float,
        aspectRatio: Float,
        minSquaresX: Float,
        minSquaresY: Float
    ) {
        batch.shader = pixelShader
        pixelShader.use {
            pixelShader.setUniformf(pixelUlProgress, progress)
            pixelShader.setUniformf(pixelUlRatio, aspectRatio)
            pixelShader.setUniformf(pixelUlSquaresMin, minSquaresX, minSquaresY)
            pixelShader.setUniformi(pixelUlSteps, 50)
        }
    }

    fun applyGrayScaleShader(batch: Batch, progress: Float) {
        batch.shader = grayScaleShader
        grayScaleShader.use {
            grayScaleShader.setUniformf(grayScaleUlDesaturation, progress)
        }
    }

    fun applyCircleCropShader(
        batch: Batch,
        progress: Float,
        aspectRatio: Float,
        centerX: Float,
        centerY: Float,
        bgColor: Color,
    ) {
        batch.shader = circleCropShader
        circleCropShader.use {
            circleCropShader.setUniformf(circleCropUlProgress, progress)
            circleCropShader.setUniformf(circleCropUlRatio, aspectRatio)
            circleCropShader.setUniformf(circleCropUlBgColor, bgColor.r, bgColor.g, bgColor.b, bgColor.a)
            circleCropShader.setUniformf(circleCropUlCenter, centerX, centerY)
        }
    }

    override fun dispose() {
        pixelShader.dispose()
        grayScaleShader.dispose()
        circleCropShader.dispose()
    }
}