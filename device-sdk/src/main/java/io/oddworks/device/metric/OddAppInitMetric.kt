package io.oddworks.device.metric

import org.json.JSONObject

class OddAppInitMetric(contentType: String? = null, contentId: String? = null, meta: JSONObject? = null) : OddMetric(contentType, contentId, meta) {

    override val action: String
        get() = OddAppInitMetric.action


    override val enabled: Boolean
        get() = OddAppInitMetric.enabled

    companion object {
        private val TAG = OddAppInitMetric::class.java.simpleName
        var action = OddMetric.ACTION_APP_INIT
        var enabled = true
    }
}