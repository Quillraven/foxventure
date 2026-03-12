package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.Animation
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
    val size: Vector2,
    val collisionRect: Rect,
    val speed: Float,
    val playMode: Animation.PlayMode,
) : Component<ProjectileRequest> {
    override fun type() = ProjectileRequest

    companion object : ComponentType<ProjectileRequest>()
}
