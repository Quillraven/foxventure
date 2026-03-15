package io.github.quillraven.foxventure.trigger

import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.AudioService
import io.github.quillraven.foxventure.component.Player
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf

class TriggerAction(
    val onStart: World.() -> Unit = {},
    val onUpdate: World.() -> Boolean = { true },
)

class Trigger(private val actions: GdxArray<TriggerAction>) {
    private var currentIndex = 0
    private var actionStarted = false
    val isDone get() = currentIndex >= actions.size

    fun update(world: World) {
        if (isDone) return

        val action = actions[currentIndex]
        if (!actionStarted) {
            actionStarted = true
            action.run { world.onStart() }
        }

        val done = action.run { world.onUpdate() }
        if (done) {
            currentIndex++
            actionStarted = false
        }
    }
}

class TriggerActionBuilder {
    var onStart: World.() -> Unit = {}
    var onUpdate: World.() -> Boolean = { true }
}

class TriggerBuilder(
    val world: World,
    val audioService: AudioService = world.inject(),
) {
    val actions = gdxArrayOf<TriggerAction>()

    fun World.player() = family { all(Player) }.single()

    fun action(block: TriggerActionBuilder.() -> Unit) {
        val actionBuilder = TriggerActionBuilder().apply(block)
        actions.add(TriggerAction(onStart = actionBuilder.onStart, onUpdate = actionBuilder.onUpdate))
    }
}

fun IntervalSystem.trigger(block: TriggerBuilder.() -> Unit): Trigger =
    Trigger(TriggerBuilder(world).apply(block).actions)
