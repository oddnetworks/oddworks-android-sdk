package io.oddworks.device.metric

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class OddViewLoadMetric(context: Context,
                        contentType: String,
                        contentId: String,
                        sessionId: String,
                        val title: String,
                        meta: JSONObject? = null) : OddMetric(context, contentType, contentId, sessionId, meta) {

    override val action: String
        get() = OddMetric.Type.VIEW_LOAD.action

    override val enabled: Boolean
        get() = OddMetric.Type.VIEW_LOAD.enabled

    override fun toJSONObject(): JSONObject {
        val json = super.toJSONObject()

        try {
            val data = json.getJSONObject("data")
            val attributes = data.getJSONObject("attributes")

            attributes.put("contentTitle", title)

            data.put("attributes", attributes)
            json.put("data", data)
        } catch (e: JSONException) {
            Log.d(OddViewLoadMetric::class.java.simpleName, e.toString())
        }

        return json
    }
}
