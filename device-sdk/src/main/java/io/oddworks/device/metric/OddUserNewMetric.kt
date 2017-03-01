package io.oddworks.device.metric

import android.content.Context
import org.json.JSONObject


class OddUserNewMetric(context: Context,
                       sessionId: String,
                       contentType: String? = null,
                       contentId: String? = null,
                       meta: JSONObject? = null) : OddMetric(context, contentType, contentId, sessionId, meta) {

    override val action: String
        get() = OddMetric.Type.USER_NEW.action

    override val enabled: Boolean
        get() = OddMetric.Type.USER_NEW.enabled
}
