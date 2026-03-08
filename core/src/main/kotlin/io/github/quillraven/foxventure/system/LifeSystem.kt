package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.system.RenderSystem.Companion.sfx
import io.github.quillraven.foxventure.ui.GameViewModel
import ktx.math.vec2
import kotlin.math.min

class LifeSystem(
    assets: AssetManager = inject(),
    private val gameViewModel: GameViewModel = inject(),
) : IteratingSystem(
    family = family { all(Life, EntityTag.ACTIVE) }
) {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]
    private val deathAnimation: GdxAnimation

    init {
        val regions = objectsAtlas.findRegions("sfx/enemy-death/idle")
        deathAnimation = GdxAnimation(1 / 15f, regions, PlayMode.NORMAL)
    }

    override fun onTickEntity(entity: Entity) {
        val life = entity[Life]
        if (life.heal > 0) {
            life.amount = min(life.maxAmount.toFloat(), life.amount + life.heal)
            life.heal = 0
            if (entity has Player) {
                gameViewModel.life = life.amount
            }
        }

        if (life.amount > 0) return

        // entity has no life left -> remove it
        if (entity hasNo Player) {
            val (position) = entity[Transform]
            val (collBox) = entity[Collision]

            val scaledSize = vec2(collBox.width * 1.25f, collBox.height * 1.25f)
            val centeredPosition = vec2(
                position.x + collBox.x + collBox.width / 2 - scaledSize.x / 2,
                position.y + collBox.y + collBox.height / 2 - scaledSize.y / 2
            )

            world.sfx(centeredPosition, scaledSize, deathAnimation)
            entity.remove()
        } else {
            entity.configure { it += EntityTag.PLAYER_DEATH }
        }
    }
}
