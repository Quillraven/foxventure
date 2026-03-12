package io.github.quillraven.foxventure.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.glutils.FileTextureData
import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.Asset.Companion.get
import io.github.quillraven.foxventure.AtlasAsset
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.ai.FleksStateMachine
import io.github.quillraven.foxventure.ai.PlayerStateIdle
import io.github.quillraven.foxventure.cfg.eagleCfg
import io.github.quillraven.foxventure.cfg.mushroomCfg
import io.github.quillraven.foxventure.cfg.piranhaCfg
import io.github.quillraven.foxventure.component.Animation
import io.github.quillraven.foxventure.component.Animation.Companion.getGdxAnimation
import io.github.quillraven.foxventure.component.AnimationType
import io.github.quillraven.foxventure.component.Attack
import io.github.quillraven.foxventure.component.Collision
import io.github.quillraven.foxventure.component.Controller
import io.github.quillraven.foxventure.component.EntityTag
import io.github.quillraven.foxventure.component.Fsm
import io.github.quillraven.foxventure.component.GdxAnimation
import io.github.quillraven.foxventure.component.Graphic
import io.github.quillraven.foxventure.component.JumpControl
import io.github.quillraven.foxventure.component.Life
import io.github.quillraven.foxventure.component.Physics
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Rect
import io.github.quillraven.foxventure.component.Tiled
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.Type
import io.github.quillraven.foxventure.component.Velocity
import io.github.quillraven.foxventure.component.Wander
import io.github.quillraven.foxventure.tiled.LoadTileObjectListener
import io.github.quillraven.foxventure.ui.GameViewModel
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.id
import ktx.tiled.isNotEmpty
import ktx.tiled.property
import ktx.tiled.propertyOrNull
import ktx.tiled.width

/**
 * Spawns entities from Tiled map objects including players, enemies, and collectibles.
 */
class SpawnSystem(
    assets: AssetManager = inject(),
    private val gameViewModel: GameViewModel = inject(),
) : IntervalSystem(enabled = false), LoadTileObjectListener {
    private val objectsAtlas = assets[AtlasAsset.OBJECTS]
    private val animationCache = mutableMapOf<String, Map<AnimationType, GdxAnimation>>()

    override fun onTick() = Unit

    override fun onLoadTileObject(
        x: Float,
        y: Float,
        mapObject: TiledMapTileMapObject,
        tile: TiledMapTile
    ) {
        val w = mapObject.width.toWorldUnits()
        val h = mapObject.height.toWorldUnits()
        val tiledType = tile.property("type", "")
        val z = mapObject.property("z", Transform.zByTiledType(tiledType))
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
            it += Tiled(id = mapObject.id)
            it += Type(tiledType)
            // life
            tile.propertyOrNull<Int>("life")?.let { amount -> it += Life(amount) }

            // collision
            if (tile.objects.isNotEmpty()) {
                val collisionDamage = tile.property("collision_damage", 1)
                val tiledRect = tile.objects.single { obj -> obj is RectangleMapObject } as RectangleMapObject
                val collisionBox = Rect.ofRectangle(tiledRect.rectangle)
                it += Collision(collisionBox, collisionDamage)
            }

            graphicEntityCfg(tile, it, atlasKey)
            physicsEntityCfg(tile, it, x, y)
            attackEntityCfg(tile, it)
            wanderCfg(tile, it)
            typeSpecificEntityCfg(tile, it, atlasKey, tiledType)
        }
    }

    private fun EntityCreateContext.wanderCfg(
        tile: TiledMapTile,
        entity: Entity,
    ) {
        (tile.properties.get("wander") as? MapProperties)?.let { wanderProps ->
            val distance = wanderProps["distance"] as Float
            val (position, size) = entity[Transform]
            entity += Wander(distance, position.x + size.x * 0.5f, stopAtCliff = true)
        }
    }

    private fun EntityCreateContext.attackEntityCfg(
        tile: TiledMapTile,
        entity: Entity,
    ) {
        (tile.properties.get("attack") as? MapProperties)?.let { attackProps ->
            entity += Attack(
                range = attackProps["range"] as Float,
                cooldown = attackProps["cooldown"] as Float,
                damage = attackProps["damage"] as Int
            )
        }
    }


    private fun EntityCreateContext.typeSpecificEntityCfg(
        tile: TiledMapTile,
        entity: Entity,
        atlasKey: String,
        tiledType: String,
    ) {
        when (tiledType) {
            "player" -> {
                entity += listOf(EntityTag.ACTIVE, EntityTag.CAMERA_FOCUS)
                val playerCmp = Player(credits = 5, gems = 0)
                entity += playerCmp
                entity += Controller()
                entity += Fsm(FleksStateMachine(world, entity, PlayerStateIdle))

                val life = entity[Life]
                gameViewModel.maxLife = life.maxAmount
                gameViewModel.life = life.amount
                gameViewModel.gems = playerCmp.gems
                gameViewModel.credits = playerCmp.credits
            }

            "enemy" -> configureEnemy(tile, entity, atlasKey)
        }
    }

    private fun EntityCreateContext.graphicEntityCfg(
        tile: TiledMapTile,
        entity: Entity,
        atlasKey: String,
    ) {
        // graphic, animation
        val regions = objectsAtlas.findRegions(atlasKey)
        entity += Graphic(regions.firstOrNull() ?: gdxError("No regions for atlas key $atlasKey"))
        if (regions.size > 1 || atlasKey.endsWith("idle")) {
            // multiple texture regions -> add Animation component
            val objectKey = atlasKey.substringBeforeLast("/")
            val gdxAnimations = animationCache.getOrPut(objectKey) {
                if (animationCache.size >= 100) {
                    animationCache.clear()
                }
                objectsAtlas.regions
                    // get all regions of the object via its key
                    .filter { region -> region.name.startsWith(objectKey) && region.index == 0 }
                    // the substring after the last '/' is the AnimationType like objects/fox/idle -> idle
                    .map { region -> AnimationType.byAtlasKey(region.name.substringAfterLast("/")) }
                    // map AnimationType to real GdxAnimation
                    .associateWith { animationType -> objectsAtlas.getGdxAnimation(objectKey, animationType) }
            }

            val idleAnimation = gdxAnimations.getOrElse(AnimationType.IDLE) {
                gdxError("No idle animation for object $atlasKey")
            }
            val animationSpeed = tile.property("animation_speed", 1f)
            entity += Animation(objectKey, idleAnimation, gdxAnimations, animationSpeed)
        }
    }

    private fun EntityCreateContext.physicsEntityCfg(
        tile: TiledMapTile,
        entity: Entity,
        x: Float,
        y: Float
    ) {
        (tile.properties.get("physics") as? MapProperties)?.let { physicsProps ->
            val maxSpeed = physicsProps["max_speed"] as Float
            val jumpImpulse = physicsProps["jump_impulse"] as Float

            entity += Physics(
                gravity = physicsProps["gravity"] as Float,
                maxFallSpeed = physicsProps["max_fall_speed"] as Float,
                jumpImpulse = jumpImpulse,
                coyoteThreshold = 0.16f,
                jumpBufferThreshold = 0.16f,
                maxSpeed = maxSpeed,
                acceleration = physicsProps["acceleration"] as Float,
                deceleration = physicsProps["deceleration"] as Float,
                skidDeceleration = physicsProps["skid_deceleration"] as Float,
                airControl = physicsProps["air_control"] as Float,
                peakGravityMultiplier = physicsProps["peak_gravity_multiplier"] as Float,
                peakVelocityThreshold = physicsProps["peak_velocity_threshold"] as Float,
                climbSpeed = physicsProps["climb_speed"] as Float,
                position = vec2(x, y),
            )
            if (maxSpeed > 0f) {
                entity += Velocity()
            }
            if (jumpImpulse > 0f) {
                entity += JumpControl()
            }
        }
    }

    private fun EntityCreateContext.configureEnemy(
        tile: TiledMapTile,
        entity: Entity,
        atlasKey: String,
    ) {
        when (val enemyType = atlasKey.substringAfter("objects/").substringBefore("/")) {
            "mushroom" -> mushroomCfg(world, tile, entity)
            "eagle" -> eagleCfg(world, tile, entity)
            "piranha" -> piranhaCfg(world, tile, entity)
            else -> gdxError("No enemy state for enemy $enemyType")
        }
    }
}