package io.github.quillraven.foxventure

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import io.github.quillraven.foxventure.system.AudioService
import io.github.quillraven.foxventure.tiled.TiledService

class ServiceLocator(
    val batch: Batch = SpriteBatch(),
    val assets: AssetManager = AssetManager(InternalFileHandleResolver()),
    val tiledService: TiledService = TiledService(assets),
    val audioService: AudioService = AudioService(),
) : Disposable {
    override fun dispose() {
        batch.dispose()
        assets.dispose()
        audioService.dispose()
    }
}