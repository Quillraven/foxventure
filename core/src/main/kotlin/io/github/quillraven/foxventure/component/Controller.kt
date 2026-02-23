package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.input.Command
import ktx.collections.GdxSet
import ktx.collections.gdxSetOf

data class Controller(
    val commands: GdxSet<Command> = gdxSetOf()
) : Component<Controller> {
    override fun type() = Controller

    fun hasCommand(command: Command) = command in commands

    companion object : ComponentType<Controller>()
}
