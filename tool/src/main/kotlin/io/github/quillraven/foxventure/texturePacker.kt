package io.github.quillraven.foxventure

import com.badlogic.gdx.tools.texturepacker.TexturePacker

fun main() {
    packCharacters()
}

private fun packCharacters() {
    val inputFolder = "../assets_raw/characters"
    val outputFolder = "../assets/graphics"
    val packageFileName = "characters"

    TexturePacker.process(inputFolder, outputFolder, packageFileName)
}
