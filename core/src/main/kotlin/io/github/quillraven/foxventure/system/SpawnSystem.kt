package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.glutils.FileTextureData
import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.PlayerStateIdle
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Box
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.tiled.LoadTileObjectListener
import ktx.app.gdxError
import ktx.collections.gdxMapOf
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.isNotEmpty
import ktx.tiled.property
import ktx.tiled.width

class SpawnSystem(
    assets: AssetManager = inject(),
) : IntervalSystem(enabled = false), LoadTileObjectListener {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]

    override fun onTick() = Unit

    override fun onLoadTileObject(
        x: Float,
        y: Float,
        mapObject: TiledMapTileMapObject,
        tile: TiledMapTile
    ) {
        val w = mapObject.width.toWorldUnits()
        val h = mapObject.height.toWorldUnits()
        val z = mapObject.property("z", 0)
        val data = tile.textureRegion.texture.textureData as FileTextureData
        // Tiled references graphics in the "collection of images" tilesets as a relative path.
        // This path is the input path for the TexturePacker tool that creates a TextureAtlas out of those single images.
        // Since we use the atlas for rendering instead of the single images, we need the atlas key instead of the path.
        // The key is the path without the input folder part (= 'graphics/sprites/') and without the file extension.
        val atlasKey = data.fileHandle.pathWithoutExtension()
            .substringAfter("graphics/sprites/") // atlas key is without TexturePacker input directory name
            .substringBeforeLast("_") // remove index -> "idle_0" becomes "idle"

        world.entity {
            it += Transform(position = vec2(x, y), size = vec2(w, h), z = z)
            it += Graphic(objectsAtlas.findRegions(atlasKey).first())

            // physics, velocity and jump control
            val physicsProps = tile.properties.get("physics")
            if (physicsProps is MapProperties) {
                val maxSpeed = physicsProps.get("max_speed", Float::class.java)
                val jumpImpulse = physicsProps.get("jump_impulse", Float::class.java)

                it += Physics(
                    gravity = physicsProps.get("gravity", Float::class.java),
                    maxFallSpeed = physicsProps.get("max_fall_speed", Float::class.java),
                    jumpImpulse = jumpImpulse,
                    coyoteThreshold = 0.08f,
                    jumpBufferThreshold = 0.08f,
                    maxSpeed = maxSpeed,
                    acceleration = physicsProps.get("acceleration", Float::class.java),
                    deceleration = physicsProps.get("deceleration", Float::class.java),
                    skidDeceleration = physicsProps.get("skid_deceleration", Float::class.java),
                    airControl = physicsProps.get("air_control", Float::class.java),
                    peakGravityMultiplier = physicsProps.get("peak_gravity_multiplier", Float::class.java),
                    peakVelocityThreshold = physicsProps.get("peak_velocity_threshold", Float::class.java),
                    climbSpeed = physicsProps.get("climb_speed", Float::class.java),
                    position = vec2(x, y),
                )
                if (maxSpeed > 0f) {
                    it += Velocity()
                }
                if (jumpImpulse > 0f) {
                    it += JumpControl()
                }
            }

            // collision
            if (mapObject.tile.objects.isNotEmpty()) {
                it += Collision(Box.ofRect((mapObject.tile.objects.single() as RectangleMapObject).rectangle))
            }

            if ("player" == mapObject.name) {
                it += EntityTag.ACTIVE
                it += Player(health = 3f)
                it += Controller()
                it += EntityTag.CAMERA_FOCUS

                val objectKey = atlasKey.substringBeforeLast("/")
                it += Animation(
                    idle = getAnimation(objectKey, "idle"),
                    gdxAnimations = gdxMapOf(
                        AnimationType.RUN to getAnimation(objectKey, "run"),
                        AnimationType.JUMP to getAnimation(objectKey, "jump"),
                        AnimationType.FALL to getAnimation(objectKey, "fall"),
                        AnimationType.CLIMB to getAnimation(objectKey, "climb"),
                    ),
                )

                it += Fsm(FleksStateMachine(world, it, PlayerStateIdle))
            }
        }
    }

    private fun getAnimation(
        objectKey: String,
        animationType: String
    ): GdxAnimation {
        var regions = objectsAtlas.findRegions("$objectKey/$animationType")
        if (regions.isEmpty) {
            regions = objectsAtlas.findRegions("$objectKey/idle")
        }
        if (regions.isEmpty) {
            gdxError("No regions found for $objectKey/$animationType")
        }

        return GdxAnimation(1 / 12f, regions, PlayMode.LOOP)
    }
}