package io.github.quillraven.foxventure

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.utils.Disposable
import io.github.quillraven.foxventure.graphic.RenderContext
import io.github.quillraven.foxventure.graphic.ShaderService
import io.github.quillraven.foxventure.tiled.TiledService

class ServiceLocator(
    val fileHandleResolver: FileHandleResolver,
    val renderContext: RenderContext = RenderContext(),
    val tiledService: TiledService = TiledService(fileHandleResolver),
    val audioService: AudioService = AudioService(),
    val shaderService: ShaderService = ShaderService(),
) : Disposable {
    override fun dispose() {
        renderContext.dispose()
        tiledService.dispose()
        audioService.dispose()
        shaderService.dispose()
    }
}