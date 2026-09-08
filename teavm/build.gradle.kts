plugins {
    buildsrc.convention.`kotlin-jvm`
    alias(libs.plugins.gdxTeaVmPlugin)
}

dependencies {
    implementation(libs.gdxTeaVmFreetype)
    implementation(project(":core"))
}

gdxTeaVM {
    assets(rootProject.file("assets"))
    reflection("com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator")

    webDefaults {
        mainClass.set("io.github.quillraven.foxventure.TeaVMLauncherKt")
        htmlTitle.set("Foxventure")
        htmlWidth.set(800)
        htmlHeight.set(600)
        serverPort.set(8080)
    }

    js {}

    wasm {
        // wasm generation sometimes needs more memory than Gradle's default 512 MiB daemon heap
        outOfProcess = true
        processMemory = 1024
    }
}
