package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class Damaged(
    val source: Entity,
    val invulnerableTime: Float, // how long do you remain invulnerable until the next damage can be applied?
    val damage: Int,
) : Component<Damaged> {
    var timer: Float = 0f

    override fun type() = Damaged

    companion object : ComponentType<Damaged>()
}