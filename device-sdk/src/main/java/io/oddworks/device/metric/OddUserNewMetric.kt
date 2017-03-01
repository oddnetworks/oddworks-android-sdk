package io.oddworks.device.metric

import org.json.JSONObject


class OddUserNewMetric(contentType: String? = null, contentId: String? = null, meta: JSONObject? = null) : OddMetric(contentType, contentId, meta) {

    override val action: String
        get() = OddUserNewMetric.action

    override val enabled: Boolean
        get() = OddUserNewMetric.enabled

    companion object {
        private val TAG = OddUserNewMetric::class.java.simpleName

        var action = OddMetric.ACTION_USER_NEW
        var enabled = false
    }
}
