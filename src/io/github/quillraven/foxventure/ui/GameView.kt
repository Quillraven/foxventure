package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
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

    init {
        setFillParent(true)
        top().left()

        val gemImage = Image(TextureRegionDrawable(skin.atlas.findRegion("gem")), Scaling.fit)
        add(gemImage).padLeft(2f).size(lifeDrawables[0].minWidth, lifeDrawables[0].minHeight)

        gemLabel = Label("x0", skin, "small_border")
        add(gemLabel).padLeft(2f).fillX().bottom().padBottom(2f)

        lifeGroup = Table(skin).also { it.top() }
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
