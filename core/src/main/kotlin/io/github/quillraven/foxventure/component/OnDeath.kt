package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class OnDeath(val action: () -> Unit) : Component<OnDeath> {
    override fun type() = OnDeath

    companion object : ComponentType<OnDeath>()
}