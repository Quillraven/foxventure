package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class ItemType(val drawable: String, val cost: Int) {
    EXTRA_CREDIT(drawable = "avatar-fox", cost = 10),
    EXTRA_HEART(drawable = "life-4", cost = 40),
}

data class Item(val type: ItemType) : Component<Item> {
    override fun type() = Item

    companion object : ComponentType<Item>()
}
