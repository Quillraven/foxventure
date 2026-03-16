package io.github.quillraven.foxventure.ui.widget

import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.tommyettinger.textra.TypingLabel

class LevelComplete(skin: Skin) : Table(skin) {
    private val titleLabel: TypingLabel = TypingLabel("", skin, "default").apply {
        color = skin.getColor("grey")
        isWrap = true
        alignment = Align.center
    }
    private val gemLabel: Label = Label("", skin, "border")

    private val leftStar = Image(null, Scaling.fit)
    private val centerStar = Image(null, Scaling.fit)
    private val rightStar = Image(null, Scaling.fit)

    init {
        setFillParent(true)

        // Panel
        val panel = Table(skin).apply { background = skin.getDrawable("button-blue-gradient") }

        // title
        panel.add(titleLabel).padTop(32f).padBottom(8f).growX().fillY().row()

        // content
        val content = Table(skin).apply { background = skin.getDrawable("button-grey-yellow") }
        content.add(Image(skin.getDrawable("gem"), Scaling.fit)).padRight(6f).size(32f)
        content.add(gemLabel).padLeft(2f).bottom().padBottom(5f)
        panel.add(content).pad(8f).grow()

        // Stars overlay — positioned relative to the stack so they overflow above the panel
        val starsOverlay = Table(skin)
        starsOverlay.top()

        // negative padTop pulls them above the stack top edge
        starsOverlay.add(leftStar).size(48f).padTop(-25f).padRight(-8f).bottom()
        starsOverlay.add(centerStar).size(64f).padTop(-38f).bottom()
        starsOverlay.add(rightStar).size(48f).padTop(-25f).padLeft(-8f).bottom()

        val stack = Stack(panel, starsOverlay)
        add(stack).center().size(240f, 180f)
    }

    fun show(gems: Int, gemsMax: Int, mapName: String) {
        updateStars(gems, gemsMax)

        titleLabel.restart("{WAVE}$mapName\n\nCOMPLETE!{RESET}")
        gemLabel.setText("$gems/$gemsMax")

        clearActions()
        color.a = 0f
        isVisible = true
        addAction(
            Actions.sequence(
                Actions.fadeIn(0.5f),
                Actions.delay(6f),
                Actions.fadeOut(0.5f),
                Actions.hide(),
            )
        )
    }

    private fun updateStars(gems: Int, gemsMax: Int) {
        val star = skin.getDrawable("star")
        val starOutline = skin.getDrawable("star-outline")

        // total gems found >= 1/3 -> one star
        leftStar.drawable = if (gems >= gemsMax * 0.33f) star else starOutline
        leftStar.setOrigin(Align.center)
        leftStar.rotation = 20f
        // total gems found >= 60% -> second star
        rightStar.drawable = if (gems >= gemsMax * 0.6f) star else starOutline
        rightStar.setOrigin(Align.center)
        rightStar.rotation = -20f
        // total gems found >= 90% -> third star
        centerStar.drawable = if (gems >= gemsMax * 0.9f) star else starOutline
        centerStar.toFront()
    }
}
