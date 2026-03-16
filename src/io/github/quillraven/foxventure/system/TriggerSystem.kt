package io.github.quillraven.foxventure.system

import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.foxventure.GdxGame.Companion.toWorldUnits
import io.github.quillraven.foxventure.PhysicsTimer
import io.github.quillraven.foxventure.component.Player
import io.github.quillraven.foxventure.component.Transform
import io.github.quillraven.foxventure.component.TriggerRef
import io.github.quillraven.foxventure.tiled.LoadTriggerListener
import io.github.quillraven.foxventure.tiled.MapChangeListener
import io.github.quillraven.foxventure.trigger.Trigger
import io.github.quillraven.foxventure.trigger.tutorial.tutorialCutscene
import io.github.quillraven.foxventure.trigger.tutorial.tutorialTrigger1
import io.github.quillraven.foxventure.trigger.tutorial.tutorialTrigger2
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width
import ktx.tiled.x
import ktx.tiled.y

class TriggerSystem(
    private val physicsTimer: PhysicsTimer = inject(),
) :
    IteratingSystem(
        family = family { all(TriggerRef) },
    ), MapChangeListener, LoadTriggerListener {

    private val playerFamily = family { all(Transform, Player) }
    private val activeTriggers = mutableListOf<Trigger>()

    override fun onTick() {
        activeTriggers.removeAll { trigger ->
            trigger.update(world)
            trigger.isDone
        }

        if (family.isEmpty || physicsTimer.numSteps == 0) return

        val (playerPosition, playerSize) = playerFamily.single()[Transform]
        family.forEach { entity ->
            val transform = entity.getOrNull(Transform)
            if (transform == null) {
                activeTriggers += entity[TriggerRef].trigger
                entity.remove()
                return@forEach
            }

            val (position, size) = entity[Transform]

            val playerOverlapping = position.x < playerPosition.x + playerSize.x
                    && position.x + size.x > playerPosition.x
                    && position.y < playerPosition.y + playerSize.y
                    && position.y + size.y > playerPosition.y
            if (!playerOverlapping) {
                return@forEach
            }

            activeTriggers += entity[TriggerRef].trigger
            entity.remove()
        }
    }

    override fun onTickEntity(entity: Entity) = Unit

    override fun onLoadTrigger(mapObject: RectangleMapObject) {
        world.entity {
            val x = mapObject.x.toWorldUnits()
            val y = mapObject.y.toWorldUnits()
            val w = mapObject.width.toWorldUnits()
            val h = mapObject.height.toWorldUnits()

            it += Transform(position = vec2(x, y), size = vec2(w, h))
            it += TriggerRef(triggerByName(mapObject.name))
        }
    }

    private fun triggerByName(name: String): Trigger = when (name) {
        "tutorial-1" -> tutorialTrigger1()
        "tutorial-2" -> tutorialTrigger2()
        else -> gdxError("Unknown trigger name $name")
    }

    override fun onMapChanged(mapName: String, tiledMap: TiledMap) {
        when (mapName) {
            "tutorial.tmx" -> activeTriggers += tutorialCutscene()
        }
    }
}
