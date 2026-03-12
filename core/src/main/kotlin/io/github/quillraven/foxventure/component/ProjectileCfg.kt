package io.github.quillraven.foxventure.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class ProjectileCfg(
    val atlasKey: String,
    val size: Vector2,
    val collisionRect: Rect,
    val speed: Float,
    val playMode: Animation.PlayMode,
    val spawnOffset: Vector2,
    val damage: Int,
) : Component<ProjectileCfg> {
    override fun type() = ProjectileCfg

    fun toRequest(source: Entity, target: Entity): ProjectileRequest {
        return ProjectileRequest(
            source = source,
            target = target,
            damage = damage,
            spawnOffset = spawnOffset,
            atlasKey = atlasKey,
            size = size,
            collisionRect = collisionRect,
            speed = speed,
            playMode = playMode,
        )
    }

    companion object : ComponentType<ProjectileCfg>()
}