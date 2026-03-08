package io.github.quillraven.foxventure

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.utils.Disposable
import io.github.quillraven.foxventure.graphic.RenderContext
import io.github.quillraven.foxventure.graphic.ShaderService
import io.github.quillraven.foxventure.tiled.TiledService

class ServiceLocator(
    val renderContext: RenderContext = RenderContext(),
    val assets: AssetManager = AssetManager(InternalFileHandleResolver()),
    val tiledService: TiledService = TiledService(assets),
    val audioService: AudioService = AudioService(),
    val shaderService: ShaderService = ShaderService(),
) : Disposable {
    override fun dispose() {
        renderContext.dispose()
        assets.dispose()
        audioService.dispose()
        shaderService.dispose()
    }
}