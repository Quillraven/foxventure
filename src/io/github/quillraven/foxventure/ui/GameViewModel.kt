package io.github.quillraven.foxventure.ui

class GameViewModel {
    var onLifeChanged: (life: Float, maxLife: Int) -> Unit = { _, _ -> }
    var onGemsChanged: (gems: Int) -> Unit = {}
    var onCreditsChanged: (credits: Int) -> Unit = {}

    var life: Float by notifying(0f) { onLifeChanged(it.coerceAtLeast(0f), maxLife) }
    var maxLife: Int by notifying(0) { onLifeChanged(life, it) }
    var gems: Int by notifying(0) { onGemsChanged(it) }
    var credits: Int by notifying(0) { onCreditsChanged(it) }
}
