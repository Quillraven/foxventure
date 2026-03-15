package io.github.quillraven.foxventure.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.foxventure.trigger.Trigger

data class TriggerRef(val trigger: Trigger) : Component<TriggerRef> {
    override fun type() = TriggerRef

    companion object : ComponentType<TriggerRef>()
}