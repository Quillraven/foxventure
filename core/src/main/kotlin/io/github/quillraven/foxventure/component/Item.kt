package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class ItemType { EXTRA_CREDIT, EXTRA_HEART }

data class Item(val type: ItemType) : Component<Item> {
    override fun type() = Item

    companion object : ComponentType<Item>()
}
