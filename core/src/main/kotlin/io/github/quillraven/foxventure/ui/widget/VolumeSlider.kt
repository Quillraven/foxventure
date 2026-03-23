package io.github.quillraven.foxventure.ui.widget

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Widget

class VolumeSlider(skin: Skin, initialValue: Float = 1f) : Widget() {

    private val background: NinePatch = skin.getPatch("slide_horizontal_grey")
    private val foreground: NinePatch = skin.getPatch("slide_horizontal_color")
    private val knob: TextureRegion = skin.getRegion("slide_hangle")

    var value: Float = initialValue.coerceIn(0f, 1f)
        private set

    var onValueChanged: (Float) -> Unit = {}

    init {
        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                updateValue(x)
                return true
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                updateValue(x)
            }
        })
    }

    private fun updateValue(touchX: Float) {
        val knobHalfW = knob.regionWidth / 2f
        val trackEnd = width - knobHalfW
        value = MathUtils.clamp((touchX - knobHalfW) / (trackEnd - knobHalfW), 0f, 1f)
        onValueChanged(value)
    }

    override fun getPrefWidth() = 128f
    override fun getPrefHeight() = knob.regionHeight.toFloat()

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        val x = x
        val y = y
        val w = width
        val h = height

        val knobW = knob.regionWidth.toFloat()
        val knobH = knob.regionHeight.toFloat()
        val knobHalfW = knobW / 2f

        val trackY = y + (h - background.totalHeight) / 2f
        val trackH = background.totalHeight

        // full gray background
        background.draw(batch, x, trackY, w, trackH)

        // color foreground clipped to knob position
        val knobX = x + knobHalfW + value * (w - knobW)
        val fillWidth = knobX - x + 7f // extend a little bit to hide "bubble" on the right of the graphic
        if (fillWidth > 0f) {
            foreground.draw(batch, x, trackY, fillWidth, trackH)
        }

        // knob centered on its position
        batch.draw(knob, knobX - knobHalfW, y + (h - knobH) / 2f, knobW, knobH)
    }
}
