package io.github.quillraven.foxventure.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class ProjectileRequest(
    val source: Entity,
    val target: Entity,
    val damage: Int,
    val spawnOffset: Vector2,
    val atlasKey: String,
    val speed: Float,
) : Component<ProjectileRequest> {
    override fun type() = ProjectileRequest

    companion object : ComponentType<ProjectileRequest>()
}
