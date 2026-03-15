package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import io.github.quillraven.foxventure.ui.widget.MessageBox
import ktx.collections.gdxArrayOf

class GameView(
    viewModel: GameViewModel,
    skin: Skin,
) : Table(skin) {
    private val lifeDrawables = gdxArrayOf(
        skin.getDrawable("life-0"),
        skin.getDrawable("life-1"),
        skin.getDrawable("life-2"),
        skin.getDrawable("life-3"),
        skin.getDrawable("life-4"),
    )
    private val lifeGroup: Table
    private val gemLabel: Label
    private val creditsLabel: Label
    private val messageBox: MessageBox

    init {
        setFillParent(true)
        top().left()

        // HUD row — all items in a nested table so they stay grouped at top-left
        val hud = Table(skin).also { it.top().left() }

        // credits
        val avatarImage = Image(skin.getDrawable("avatar-fox"), Scaling.fit)
        hud.add(avatarImage).padLeft(2f)
        creditsLabel = Label("x0", skin, "small_border")
        hud.add(creditsLabel).padLeft(2f).bottom().padBottom(2f)

        // gems
        val gemImage = Image(skin.getDrawable("gem"), Scaling.fit)
        hud.add(gemImage).padLeft(10f)
        gemLabel = Label("x0", skin, "small_border")
        hud.add(gemLabel).padLeft(2f).bottom().padBottom(2f)

        // life
        lifeGroup = Table(skin).also { it.top().left() }
        hud.add(lifeGroup).padLeft(20f)

        add(hud).left().row()

        // message box
        messageBox = MessageBox(skin)
        messageBox.isVisible = false
        add(messageBox).grow().height(150f).padLeft(160f).padRight(160f).padBottom(150f)

        viewModel.onLifeChanged = this::onLifeChanged
        viewModel.onGemsChanged = { gems -> gemLabel.setText("x$gems") }
        viewModel.onCreditsChanged = { credits -> creditsLabel.setText("x$credits") }
        viewModel.onShowMessage = { drawableName, text ->
            messageBox.isVisible = true
            messageBox.setMessage(drawableName, text)
        }
        viewModel.onHideMessage = { messageBox.isVisible = false }
    }

    private fun onLifeChanged(life: Float, maxLife: Int) {
        val numHearts = (maxLife + 3) / 4

        if (lifeGroup.children.size != numHearts) {
            // Add or remove heart images as needed
            while (lifeGroup.children.size < numHearts) {
                val image = Image(null, Scaling.fit)
                lifeGroup.add(image).padLeft(2f)
            }
            while (lifeGroup.children.size > numHearts) {
                lifeGroup.removeActorAt(lifeGroup.children.size - 1, true)
            }
        }

        // Update each heart's drawable
        val lifeInt = life.toInt()
        lifeGroup.children.forEachIndexed { index, image ->
            val heartLife = (lifeInt - index * 4).coerceIn(0, 4)
            (image as Image).drawable = lifeDrawables[heartLife]
        }
    }
}
