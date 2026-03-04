package io.github.quillraven.foxventure.system

data class PhysicsTimer(
    val interval: Float,
    private var accumulator: Float = 0f,
) {
    var alpha: Float = 0f
        private set

    var numSteps: Int = 0
        private set

    fun update(deltaTime: Float) {
        numSteps = 0
        accumulator += deltaTime
        while (accumulator >= interval) {
            accumulator -= interval
            ++numSteps
        }
        alpha = accumulator / interval
    }
}