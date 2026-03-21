package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Item
import io.github.quillraven.foxventure.component.ItemType
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.ui.GameViewModel

class ItemSystem(
    private val gameViewModel: GameViewModel = inject(),
) : IteratingSystem(family = family { all(Item) }) {

    private val playerFamily = family { all(Player) }

    override fun onTickEntity(entity: Entity) {
        val player = playerFamily.single()
        val playerCmp = player[Player]

        when (entity[Item].type) {
            ItemType.EXTRA_CREDIT -> {
                if (playerCmp.gems < 10) return
                playerCmp.gems -= 10
                playerCmp.credits++
                gameViewModel.gems = playerCmp.gems
                gameViewModel.credits = playerCmp.credits
            }

            ItemType.EXTRA_HEART -> {
                if (playerCmp.gems < 40) return
                playerCmp.gems -= 40
                val playerLife = player[Life]
                playerLife.maxAmount += 4
                playerLife.amount += 4
                gameViewModel.gems = playerCmp.gems
                gameViewModel.life = playerLife.amount
                gameViewModel.maxLife = playerLife.maxAmount
            }
        }

        entity.remove()
    }
}
