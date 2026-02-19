package io.github.quillraven.foxventure

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import ktx.assets.assetDescriptor

enum class MusicAsset {
    HURT_AND_HEART;

    val descriptor: AssetDescriptor<Music> = assetDescriptor<Music>("music/${name.lowercase()}.ogg")
}

operator fun AssetManager.get(asset: MusicAsset): Music = get(asset.descriptor)

enum class AtlasAsset {
    OBJECTS;

    val descriptor: AssetDescriptor<TextureAtlas> = assetDescriptor("graphics/${name.lowercase()}.atlas")
}

operator fun AssetManager.get(asset: AtlasAsset): TextureAtlas = get(asset.descriptor)

enum class MapAsset {
    TUTORIAL;

    val descriptor: AssetDescriptor<TiledMap> = assetDescriptor("maps/${name.lowercase()}.tmx", defaultParams())

    private fun defaultParams(): BaseTiledMapLoader.Parameters {
        return BaseTiledMapLoader.Parameters().apply {
            projectFilePath = "maps/foxventure.tiled-project"
        }
    }
}

operator fun AssetManager.get(asset: MapAsset): TiledMap = get(asset.descriptor)
