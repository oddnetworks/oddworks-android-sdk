package io.oddworks.device.model.common

data class OddColor(val red: Int, val green: Int, val blue: Int, val alpha: Int, val label: String) {
    init {
        // TODO - ensure values within 0-255 range
    }
}
