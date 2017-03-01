package io.oddworks.device.metric

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class OddVideoPlayMetric(contentType: String, contentId: String, val title: String, meta: JSONObject? = null) : OddMetric(contentType, contentId, meta) {

    override val action: String
        get() = OddVideoPlayMetric.action

    override val enabled: Boolean
        get() = OddVideoPlayMetric.enabled

    override fun toJSONObject(): JSONObject {
        val json = super.toJSONObject()

        try {
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")

            attributes.put("contentTitle", title)

            data.put("attributes", attributes)
            json.put("data", data)
        } catch (e: JSONException) {
            Log.d(TAG, e.toString())
        }

        return json
    }

    companion object {
        private val TAG = OddVideoPlayMetric::class.java.simpleName

        var action = OddMetric.ACTION_VIDEO_PLAY
        var enabled = false
    }
}