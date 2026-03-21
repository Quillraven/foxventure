package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.collections.GdxArray

data class Shop(val items: GdxArray<ItemType>) : Component<Shop> {
    override fun type() = Shop

    companion object : ComponentType<Shop>()
}
