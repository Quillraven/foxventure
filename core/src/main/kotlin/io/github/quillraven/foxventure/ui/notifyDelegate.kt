package io.github.quillraven.foxventure.ui

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

fun <T> notifying(initial: T, onChange: (T) -> Unit): ReadWriteProperty<Any?, T> =
    Delegates.observable(initial) { _, old, new -> if (old != new) onChange(new) }