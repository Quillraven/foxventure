package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

/**
 * Tags for categorizing entities by their state or role.
 */
enum class EntityTag : EntityTags by entityTagOf() {
    ACTIVE,
    CAMERA_FOCUS,
    CLIMBING,
    ROOT,
    PLAYER_DEATH,
    PROJECTILE,
    BOSS,
}