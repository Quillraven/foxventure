package io.github.quillraven.foxventure

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.assets.assetDescriptor

interface Asset<T> {
    val descriptor: AssetDescriptor<T>

    companion object {
        inline operator fun <reified T> AssetManager.get(asset: Asset<T>): T = get(asset.descriptor)
    }
}

enum class AtlasAsset(path: String) : Asset<TextureAtlas> {
    OBJECTS("graphics/objects.atlas");

    override val descriptor = assetDescriptor<TextureAtlas>(path)
}

enum class MapAsset : Asset<TiledMap> {
    TUTORIAL;

    override val descriptor = assetDescriptor("maps/${name.lowercase()}.tmx", defaultParams())

    private fun defaultParams(): BaseTiledMapLoader.Parameters {
        return BaseTiledMapLoader.Parameters().apply {
            projectFilePath = "maps/foxventure.tiled-project"
        }
    }
}

enum class SkinAsset : Asset<Skin> {
    UI;

    override val descriptor = assetDescriptor<Skin>("ui/${name.lowercase()}.json")
}
