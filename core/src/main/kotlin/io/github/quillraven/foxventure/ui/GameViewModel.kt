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

    var gems: Int = 0
        set(value) {
            if (field != value) {
                field = value
                onGemsChanged?.invoke(field)
            }
        }

    var credits: Int = 0
        set(value) {
            if (field != value) {
                field = value
                onCreditsChanged?.invoke(field)
            }
        }

    var onLifeChanged: ((life: Int, maxLife: Int) -> Unit)? = null

    var onGemsChanged: ((gems: Int) -> Unit)? = null

    var onCreditsChanged: ((credits: Int) -> Unit)? = null
}
