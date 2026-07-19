plugins {
    buildsrc.convention.`kotlin-jvm`
    alias(libs.plugins.gdxTeaVmPlugin)
}

dependencies {
    implementation(libs.gdxTeaVmFreetype)
    implementation(project(":core"))
    implementation(libs.stripeFreetype)
}

gdxTeaVM {
    assets(rootProject.file("assets"))
    reflection("com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator")

    js {
        mainClass.set("io.github.quillraven.foxventure.TeaVMLauncherKt")
        htmlTitle.set("Foxventure JS")
        htmlWidth.set(800)
        htmlHeight.set(600)
        serverPort.set(8080)
        obfuscated.set(false)
    }

    wasm {
        mainClass.set("io.github.quillraven.foxventure.TeaVMLauncherKt")
        htmlTitle.set("Foxventure Wasm")
        htmlWidth.set(700)
        htmlHeight.set(800)
        serverPort.set(8080)
        obfuscated.set(false)
    }
}
