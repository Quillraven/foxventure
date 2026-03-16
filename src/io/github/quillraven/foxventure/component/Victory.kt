package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Victory(val housePosition: Vector2) : Component<Victory> {
    override fun type() = Victory

    companion object : ComponentType<Victory>()
}
