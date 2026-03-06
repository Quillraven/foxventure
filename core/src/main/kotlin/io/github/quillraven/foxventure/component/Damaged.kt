package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class Damaged(
    val source: Entity,
    val damage: Int,
    val soundName: String,
    val pushBackForce: Float,
) : Component<Damaged> {
    override fun type() = Damaged

    companion object : ComponentType<Damaged>()
}