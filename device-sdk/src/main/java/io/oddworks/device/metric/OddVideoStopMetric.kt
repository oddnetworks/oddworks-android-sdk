package io.oddworks.device.metric

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

class OddVideoStopMetric(contentType: String, contentId: String, val title: String, meta: JSONObject? = null, val elapsed: Int = 0, val duration: Int = 0) : OddMetric(contentType, contentId, meta) {

    override val action: String
        get() = OddVideoStopMetric.action

    override val enabled: Boolean
        get() = OddVideoStopMetric.enabled

    override fun toJSONObject(): JSONObject {
        val json = super.toJSONObject()

        try {
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")

            attributes.put("contentTitle", title)
            attributes.put("elapsed", elapsed)
            attributes.put("duration", duration)

            data.put("attributes", attributes)
            json.put("data", data)
        } catch (e: JSONException) {
            Log.d(TAG, e.toString())
        }

        return json
    }

    override fun toString(): String {
        return "$TAG (type=$type, contentType=$contentType, contentId=$contentId, meta=${meta.toString()}, elapsed=$elapsed, duration=$duration)"
    }

    companion object {
        private val TAG = OddVideoStopMetric::class.java.simpleName

        var action = OddMetric.ACTION_VIDEO_STOP
        var enabled = false
    }
}
