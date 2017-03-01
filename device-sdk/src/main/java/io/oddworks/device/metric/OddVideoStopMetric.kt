package io.oddworks.device.metric

import android.content.Context
import android.util.Log

import org.json.JSONException
import org.json.JSONObject

class OddVideoStopMetric(context: Context,
                         contentType: String,
                         contentId: String,
                         sessionId: String,
                         val videoSessionId: String,
                         val title: String,
                         meta: JSONObject? = null,
                         val elapsed: Int = 0,
                         val duration: Int = 0) : OddMetric(context, contentType, contentId, sessionId, meta) {

    override val action: String
        get() = OddMetric.Type.VIDEO_STOP.action

    override val enabled: Boolean
        get() = OddMetric.Type.VIDEO_STOP.enabled

    override fun toJSONObject(): JSONObject {
        val json = super.toJSONObject()

        try {
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")

            attributes.put("videoSessionId", videoSessionId)
            attributes.put("contentTitle", title)
            attributes.put("elapsed", elapsed)
            attributes.put("duration", duration)

            data.put("attributes", attributes)
            json.put("data", data)
        } catch (e: JSONException) {
            Log.d(OddVideoStopMetric::class.java.simpleName, e.toString())
        }

        return json
    }

    override fun toString(): String {
        return "${OddVideoStopMetric::class.java.simpleName} (type=$type, contentType=$contentType, contentId=$contentId, meta=${meta.toString()}, elapsed=$elapsed, duration=$duration)"
    }
}
