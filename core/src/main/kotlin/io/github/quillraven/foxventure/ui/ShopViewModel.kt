package io.github.quillraven.foxventure.ui

import com.github.quillraven.fleks.World
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.Item
import io.github.quillraven.foxventure.component.ItemType
import io.github.quillraven.foxventure.component.Player

class ShopViewModel {
    lateinit var world: World

    var onOpenShop: () -> Unit = {}

    fun onPurchase(itemType: ItemType) {
        world.entity { it += Item(itemType) }
    }

    fun closeShop() {
        world.family { all(Player) }.forEach {
            it.configure { player -> player += Controller() }
        }
    }
}
