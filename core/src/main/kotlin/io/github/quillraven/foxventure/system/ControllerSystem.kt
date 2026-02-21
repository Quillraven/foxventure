package io.github.quillraven.foxventure.system

import com.badlogic.gdx.Input
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.input.Command
import ktx.app.KtxInputAdapter
import ktx.collections.gdxArrayOf
import ktx.collections.removeAll

class ControllerSystem : IteratingSystem(family = family { all(Controller, EntityTag.ACTIVE) }), KtxInputAdapter {
    private val keyboardMapping = mapOf(
        Input.Keys.W to Command.MOVE_UP,
        Input.Keys.A to Command.MOVE_LEFT,
        Input.Keys.S to Command.MOVE_DOWN,
        Input.Keys.D to Command.MOVE_RIGHT,
        Input.Keys.SPACE to Command.JUMP,
    )
    private val commandsToAdd = gdxArrayOf<Command>()
    private val commandsToRemove = gdxArrayOf<Command>()

    override fun keyDown(keycode: Int): Boolean {
        keyboardMapping[keycode]?.let { command ->
            commandsToAdd.add(command)
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        keyboardMapping[keycode]?.let { command ->
            commandsToRemove.add(command)
            return true
        }
        return false
    }

    override fun onTick() {
        super.onTick()
        commandsToAdd.clear()
        commandsToRemove.clear()
    }

    override fun onTickEntity(entity: Entity) {
        val controller = entity[Controller]
        controller.commands.addAll(commandsToAdd)
        controller.commands.removeAll(commandsToRemove)
    }
}
