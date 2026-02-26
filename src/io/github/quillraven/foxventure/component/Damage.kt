package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Damage(
    val amount: Int,
) : Component<Damage> {
    override fun type() = Damage

    companion object : ComponentType<Damage>()
}
