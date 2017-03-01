package io.oddworks.device.metric

import android.content.Context
import org.json.JSONObject

class OddAppInitMetric(context: Context,
                       contentType: String? = null,
                       contentId: String? = null,
                       sessionId: String,
                       meta: JSONObject? = null) : OddMetric(context, contentType, contentId, sessionId, meta) {

    override val action: String
        get() = OddMetric.Type.APP_INIT.action


    override val enabled: Boolean
        get() = OddMetric.Type.APP_INIT.enabled
}