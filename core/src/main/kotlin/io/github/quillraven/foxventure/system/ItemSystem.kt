package io.github.quillraven.foxventure.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.component.Item
import io.github.quillraven.foxventure.component.ItemType
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.screen.GameScreen
import io.github.quillraven.foxventure.ui.GameViewModel

class ItemSystem(
    private val gameViewModel: GameViewModel = inject(),
) : IteratingSystem(family = family { all(Item) }) {

    private val playerFamily = family { all(Player) }

    override fun onTickEntity(entity: Entity) {
        val player = playerFamily.single()
        val playerCmp = player[Player]
        val itemType = entity[Item].type

        if (playerCmp.gems < itemType.cost) {
            // cannot afford item -> do nothing
            entity.remove()
            return
        }

        // consume item -> modify player stats
        playerCmp.gems -= itemType.cost
        gameViewModel.gems = playerCmp.gems
        val life = player[Life]

        when (itemType) {
            ItemType.EXTRA_CREDIT -> {
                playerCmp.credits++
                gameViewModel.credits = playerCmp.credits
            }
            ItemType.EXTRA_HEART -> {
                life.maxAmount += 4
                life.amount += 4
                gameViewModel.life = life.amount
                gameViewModel.maxLife = life.maxAmount
            }
        }

        // also set global player stats in case the player dies and respawns
        GameScreen.playerCredits = playerCmp.credits
        GameScreen.playerGems = playerCmp.gems
        GameScreen.playerLife = life.amount
        GameScreen.playerLifeMax = life.maxAmount

        // remove item
        entity.remove()
    }
}
