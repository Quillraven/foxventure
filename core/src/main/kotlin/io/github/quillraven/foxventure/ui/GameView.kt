package io.github.quillraven.foxventure.ui

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling

class GameView(
    viewModel: GameViewModel,
    uiAtlas: TextureAtlas,
) : Table() {
    private val lifeDrawables = uiAtlas.findRegions("life").map { TextureRegionDrawable(it) }

    private val lifeImage = Image(null, Scaling.fit)

    init {
        setFillParent(true)
        top().left()
        add(lifeImage).padLeft(2f)

        viewModel.onLifeChanged = { life ->
            lifeImage.drawable = lifeDrawables[life]
        }
    }
}
