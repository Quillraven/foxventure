package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class Damage(
    val source: Entity,
    val amount: Int,
) : Component<Damage> {
    override fun type() = Damage

    companion object : ComponentType<Damage>()
}
