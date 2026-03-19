package io.github.quillraven.foxventure.ui

import com.github.tommyettinger.textra.Effect
import com.github.tommyettinger.textra.TypingLabel

class ArcEffect(label: TypingLabel, params: Array<String>) : Effect(label) {
    private val height = if (params.isNotEmpty()) paramAsFloat(params[0], 1f) else 1f

    override fun onApply(glyph: Long, localIndex: Int, globalIndex: Int, delta: Float) {
        val end = if (indexEnd < 0) label.length() else indexEnd
        val count = (end - indexStart).coerceAtLeast(2)
        val t = localIndex.toFloat() / (count - 1)
        val yOffset = label.getLineHeight(globalIndex) * height * 4f * t * (1f - t)
        label.offsets.incr(globalIndex shl 1 or 1, yOffset)
    }
}
