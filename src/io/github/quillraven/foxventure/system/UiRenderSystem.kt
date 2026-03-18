package io.github.quillraven.foxventure.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.screen.GameScreen
import io.github.quillraven.foxventure.ui.widget.PauseOverlay

/**
 * Renders the UI stage including HUD and menus.
 */
class UiRenderSystem(
    private val stage: Stage = inject(),
    skin: Skin = inject(),
) : IntervalSystem() {
    private val pauseOverlay = PauseOverlay(skin)

    override fun onTick() {
        if (GameScreen.gamePaused && pauseOverlay.parent == null) {
            stage.addActor(pauseOverlay)
        } else if (!GameScreen.gamePaused && pauseOverlay.parent != null) {
            stage.root.removeActor(pauseOverlay)
        }

        stage.batch.shader = null
        stage.batch.color = Color.WHITE
        stage.viewport.apply()
        stage.act(deltaTime)
        stage.draw()
    }

    override fun onDispose() {
        pauseOverlay.dispose()
    }
}
