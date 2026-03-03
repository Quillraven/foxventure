package io.github.quillraven.foxventure.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject

class UiRenderSystem(
    private val stage: Stage = inject(),
) : IntervalSystem() {
    override fun onTick() {
        stage.viewport.apply()
        stage.act(deltaTime)
        stage.draw()
    }
}