package io.github.quillraven.foxventure.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.input.Command

class ControllerSystem : IteratingSystem(
    family = family { all(Controller) }
) {
    override fun onTickEntity(entity: Entity) {
        val controller = entity[Controller]
        controller.commands.clear()

        // Map keyboard input to commands
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            controller.commands.add(Command.MOVE_LEFT)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            controller.commands.add(Command.MOVE_RIGHT)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            controller.commands.add(Command.MOVE_UP)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            controller.commands.add(Command.MOVE_DOWN)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            controller.commands.add(Command.JUMP)
        }
    }
}
