package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Animation.Companion.getGdxAnimation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Damage
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.Physics.Companion.projectilePhysics
import io.github.quillraven.foxventure.component.ProjectileRequest
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Velocity
import ktx.math.vec2

class ProjectileRequestSystem(
    assets: AssetManager = inject(),
) : IteratingSystem(
    family = family { all(ProjectileRequest) }
) {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]

    override fun onTickEntity(entity: Entity) {
        val (source, target, damage, spawnOffset, atlasKey, projectileSize, collisionRect, speed, playMode) = entity[ProjectileRequest]
        if (source.wasRemoved() || target.wasRemoved()) {
            entity.remove()
            return
        }

        val targetTransform = target[Transform]
        val targetCollBox = target[Collision].box
        val targetCenterX = targetTransform.position.x + targetCollBox.x + targetCollBox.width * 0.5f

        val (sourcePosition, sourceSize, _, _, sourceZ) = source[Transform]
        val sourceCollBox = source[Collision].box
        val sourceX = sourcePosition.x
        val sourceCenterX = sourceX + sourceCollBox.x + sourceCollBox.width * 0.5f

        val targetIsLeft = targetCenterX < sourceCenterX
        val projectileX = when {
            targetIsLeft -> sourceX + sourceSize.x - spawnOffset.x - projectileSize.x
            else -> sourceX + spawnOffset.x
        }
        val projectileY = sourcePosition.y + spawnOffset.y - projectileSize.y * 0.5f
        val projectilePosition = vec2(projectileX, projectileY)

        world.entity {
            val gdxAnimation = objectsAtlas.getGdxAnimation(atlasKey, AnimationType.IDLE, playMode)

            it += Transform(projectilePosition, projectileSize.cpy(), z = sourceZ - 1)
            it += Graphic(gdxAnimation.getKeyFrame(0f), flip = targetIsLeft)
            it += Animation(objectKey = atlasKey, gdxAnimation, gdxAnimations = emptyMap(), speed = 1f)
            it += Velocity().apply { current.x = if (targetIsLeft) -speed else speed }
            it += projectilePhysics(projectilePosition.cpy(), speed)
            it += Collision(box = collisionRect, collisionDamage = damage)
            it += Damage(source = source, amount = damage)
            it += Type("damage")
            it += EntityTag.ACTIVE
            it += EntityTag.PROJECTILE
        }
        entity.remove()
    }
}
