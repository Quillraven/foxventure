package io.github.quillraven.foxventure

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.assets.assetDescriptor

enum class MusicAsset {
    HURT_AND_HEART;

    val descriptor: AssetDescriptor<Music> = assetDescriptor<Music>("music/${name.lowercase()}.ogg")
}

operator fun AssetManager.get(asset: MusicAsset): Music = get(asset.descriptor)

enum class AtlasAsset {
    CHARACTERS;

    val descriptor: AssetDescriptor<TextureAtlas> = assetDescriptor("graphics/${name.lowercase()}.atlas")
}

operator fun AssetManager.get(asset: AtlasAsset): TextureAtlas = get(asset.descriptor)
