package io.github.quillraven.foxventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.tommyettinger.textra.TypingLabel

class MessageBox(
    skin: Skin,
) : Table(skin) {
    private val image = Image(null, Scaling.fit)
    private val messageLabel = TypingLabel("", skin, "default").apply {
        alignment = Align.topLeft
        isWrap = true
    }
    private val imageCell: Cell<Table>

    init {
        background = skin.getDrawable("label-bgd")
        val imageWithBorder = Table(skin).apply {
            background = skin.getDrawable("label-bgd")
            add(image).pad(2f).grow()
        }
        imageCell = add(imageWithBorder).pad(5f).top().left().size(48f)
        add(messageLabel).pad(10f, 15f, 10f, 10f).grow().align(Align.topLeft)
    }

    fun setMessage(drawableName: String, message: String) {
        if (drawableName.isBlank()) {
            imageCell.size(0f).pad(0f)
            imageCell.actor.isVisible = false
        } else {
            imageCell.size(48f).pad(5f)
            imageCell.actor.isVisible = true
            image.drawable = skin.getDrawable(drawableName)
        }
        invalidate()
        messageLabel.restart(message)
    }
}
