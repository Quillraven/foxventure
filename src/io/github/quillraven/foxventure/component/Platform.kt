package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.tiled.GroundTile

data class Platform(val groundTile: GroundTile) : Component<Platform> {
    override fun type() = Platform

    companion object : ComponentType<Platform>()
}