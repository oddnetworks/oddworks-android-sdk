package io.oddworks.device.model.config

import android.util.Log
import io.oddworks.device.metric.OddMetric
import io.oddworks.device.model.common.Sharing
import io.oddworks.device.model.config.features.Authentication

data class Features(val authentication: Authentication, val sharing: Sharing, val metrics: Set<OddMetric.Type>, val metricsEnabled: Boolean) {
    init {
        Log.d(Features::class.java.simpleName, "authentication[type: ${authentication.type}] sharing[enabled: ${sharing.enabled}] metrics[enabled: $metricsEnabled, total: ${metrics.size}] ")
    }
}