package io.github.quillraven.foxventure.trigger

import com.github.quillraven.fleks.IntervalSystem
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.Player

fun IntervalSystem.onTutorialMapLoad() {
    val player = world.family { all(Player) }.single()
    player.configure { it -= Controller }
}