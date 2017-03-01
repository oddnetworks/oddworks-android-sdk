package io.oddworks.device.metric

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

class OddVideoPlayingMetric(contentType: String, contentId: String, val title: String, meta: JSONObject? = null, val elapsed: Int = 0, val duration: Int = 0) : OddMetric(contentType, contentId, meta) {

    override val action: String
        get() = OddVideoPlayingMetric.action

    override val enabled: Boolean
        get() = OddVideoPlayingMetric.enabled

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
        private val TAG = OddVideoPlayingMetric::class.java.simpleName

        val DEFAULT_INTERVAL = 10000

        var action = OddMetric.ACTION_VIDEO_PLAYING
        var enabled = false

        var interval = DEFAULT_INTERVAL
    }
}
