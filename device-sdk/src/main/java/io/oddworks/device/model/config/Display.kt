package io.oddworks.device.model.config

import android.util.Log
import io.oddworks.device.model.common.OddColor
import io.oddworks.device.model.common.OddFont
import io.oddworks.device.model.common.OddImage
import io.oddworks.device.model.common.OddImageable

data class Display (override val images: Set<OddImage>, val colors: Set<OddColor>, val fonts: Set<OddFont>): OddImageable {
    init {
        Log.d(Display::class.java.simpleName, "images[total: ${images.size}] colors[total: ${colors.size}] fonts[total: ${fonts.size}]")
    }
}
