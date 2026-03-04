package io.github.quillraven.foxventure

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    Lwjgl3Application(GdxGame(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("GdxGame")
        val displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode()
        setWindowedMode(displayMode.width, displayMode.height)
        setDecorated(false)
        useVsync(true)
        setForegroundFPS(displayMode.refreshRate + 1)
        setWindowIcon("logo-128.png", "logo-64.png", "logo-32.png")
    })
}
