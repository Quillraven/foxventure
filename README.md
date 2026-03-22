# Gdx Kotlin Template

This project uses a modern [Gradle](https://gradle.org/) setup approach with Kotlin DSL,
version catalog (see `gradle/libs.versions.toml`) and extracted build logic
to convention plugins located in `buildSrc`.
It also uses both a build cache and a configuration cache (see `gradle.properties`).

It is a starting point for any [LibGDX](https://github.com/libgdx/libgdx) Kotlin application
with [LibKTX](https://github.com/libktx/ktx) extensions and provides two launchers:

- `Desktop` (=lwjgl3): `Lwjgl3Launcher.kt`
- `TeaVM` (=browser): `TeaVMLauncher.kt`

To run `lwjgl3` just execute the `main` method of the launcher class or run:

- `./gradlew lwjgl3:run`

[Construo](https://github.com/fourlastor-alexandria/construo) is used to package your game for distribution.
To package for Linux or Windows run:

- `./gradlew lwjgl3:packageLinuxX64`
- `./gradlew lwjgl3:packageWinX64`

For `teavm` there are four different tasks to build and optionally run it on `http://localhost:8080/`:

- `./gradlew teavm:teavmDebugBuild`: build an unobfuscated version with source-mapping for browser debugging
- `./gradlew teavm:teavmDebugRun`: run the debug build locally
- `./gradlew teavm:teavmReleaseBuild`: build an obfuscated and optimized version
- `./gradlew teavm:teavmReleaseRun`: run the release build locally

This template can be configured
using [Gdx-Quilly-Utils](https://quillraven.github.io/gdx-quilly-utils/gradle-kotlin-template).

Credits:
- https://pimen.itch.io/smoke-vfx-1
- https://ansimuz.itch.io/sunny-land-pixel-game-art
- https://sfxr.me/
- https://pixabay.com/sound-effects/film-special-effects-game-over-38511/
- https://ansimuz.itch.io/sunnyland-enemies-extended-pack
- https://fonts.google.com/specimen/Press+Start+2P
- UI: https://danieldiggle.itch.io/sunnyside
- https://pixabay.com/de/music/videospiele-game-8-bit-on-short-278081/
- https://kenney.nl/assets/ui-pack
- https://gl-transitions.com/gallery
- https://wyver9.itch.io/8-bit-beatem-up-soundtrack
- https://xdeviruchi.itch.io/8-bit-fantasy-adventure-music-pack
- https://opengameart.org/content/8bit-action-jingle-mini-loop
- https://opengameart.org/content/512-sound-effects-8-bit-style