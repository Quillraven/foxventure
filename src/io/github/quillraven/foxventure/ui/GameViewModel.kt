package io.github.quillraven.foxventure.ui

class GameViewModel {
    var life: Int = 0
        set(value) {
            if (field != value) {
                field = value.coerceAtLeast(0)
                onLifeChanged?.invoke(field, maxLife)
            }
        }

    var maxLife: Int = 0
        set(value) {
            if (field != value) {
                field = value
                onLifeChanged?.invoke(life, field)
            }
        }

    var onLifeChanged: ((life: Int, maxLife: Int) -> Unit)? = null
}
