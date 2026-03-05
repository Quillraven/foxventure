package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling

class GameView(
    viewModel: GameViewModel,
    uiAtlas: TextureAtlas,
    font: BitmapFont,
) : Table() {
    private val lifeDrawables = uiAtlas.findRegions("life").map { TextureRegionDrawable(it) }
    private val lifeGroup: Table
    private val gemLabel: Label

    init {
        setFillParent(true)
        top().left()

        val gemImage = Image(TextureRegionDrawable(uiAtlas.findRegion("gem")), Scaling.fit)
        add(gemImage).padLeft(2f).size(lifeDrawables[0].minWidth, lifeDrawables[0].minHeight)

        gemLabel = Label("x0", Label.LabelStyle(font, Color.WHITE))
        add(gemLabel).padLeft(2f).fillX().bottom().padBottom(2f)

        lifeGroup = Table().also { it.top() }
        add(lifeGroup).padLeft(10f)

        viewModel.onLifeChanged = this::onLifeChanged
        viewModel.onGemsChanged = { gems -> gemLabel.setText("x$gems") }
    }

    private fun onLifeChanged(life: Int, maxLife: Int) {
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
        lifeGroup.children.forEachIndexed { index, image ->
            val heartLife = (life - index * 4).coerceIn(0, 4)
            (image as Image).drawable = lifeDrawables[heartLife]
        }
    }
}
