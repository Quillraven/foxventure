plugins {
    buildsrc.convention.`kotlin-jvm`
}

dependencies {
    implementation(libs.gdxTools)
}

tasks.register<JavaExec>("packTextures") {
    group = "foxventure"
    description = "Packs textures of the assets/graphics/sprites folder into texture atlases"

    mainClass.set("io.github.quillraven.foxventure.TexturePackerKt")
    classpath = sourceSets.main.get().runtimeClasspath
}
