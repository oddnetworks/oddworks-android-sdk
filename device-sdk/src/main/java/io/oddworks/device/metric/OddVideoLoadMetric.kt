package io.oddworks.device.metric

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject


class OddVideoLoadMetric(context: Context,
                         contentType: String,
                         contentId: String,
                         sessionId: String,
                         val videoSessionId: String,
                         val title: String,
                         meta: JSONObject? = null) : OddMetric(context, contentType, contentId, sessionId, meta) {

    override val action: String
        get() = OddMetric.Type.VIDEO_LOAD.action

    override val enabled: Boolean
        get() = OddMetric.Type.VIDEO_LOAD.enabled

    override fun toJSONObject(): JSONObject {
        val json = super.toJSONObject()

        try {
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")

            attributes.put("videoSessionId", videoSessionId)
            attributes.put("contentTitle", title)

            data.put("attributes", attributes)
            json.put("data", data)
        } catch (e: JSONException) {
            Log.d(OddVideoLoadMetric::class.java.simpleName, e.toString())
        }

        return json
    }
}
