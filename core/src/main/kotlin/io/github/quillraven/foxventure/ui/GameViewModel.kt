package io.github.quillraven.foxventure.ui

class GameViewModel {
    var life: Int = 0
        set(value) {
            if (field != value) {
                field = value
                onLifeChanged?.invoke(value)
            }
        }

    var onLifeChanged: ((Int) -> Unit)? = null
}
