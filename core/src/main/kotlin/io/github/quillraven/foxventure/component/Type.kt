package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Type(val type: String) : Component<Type> {
    override fun type() = Type

    companion object : ComponentType<Type>()
}