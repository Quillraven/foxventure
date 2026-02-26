package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class EntityTag : EntityTags by entityTagOf() {
    ACTIVE,
    CAMERA_FOCUS,
    CLIMBING,
    ROOT,
}