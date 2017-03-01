package io.oddworks.device.metric

import android.os.Build
import android.util.Log
import io.oddworks.device.service.OddRxBus
import org.json.JSONException
import org.json.JSONObject

abstract class OddMetric(protected val contentType: String?, protected val contentId: String?, protected val meta: JSONObject?) : OddRxBus.OddRxBusEvent {
    abstract val action: String
    abstract val enabled: Boolean

    val viewerId: String
        get() {
            return Build.SERIAL
        }

    val type: String
        get() = TYPE

    open fun toJSONObject(): JSONObject {
        val json = JSONObject()
        val attributes = JSONObject()

        try {
            val data = JSONObject()
            data.put("type", type)

            attributes.put("action", action)
            attributes.put("viewer", viewerId)
            attributes.put("contentType", contentType)
            attributes.put("contentId", contentId)

            data.put("attributes", attributes)
            data.put("meta", meta)
            json.put("data", data);
        } catch (e: JSONException) {
            Log.d(TAG, e.toString())
        }

        return json
    }

    override fun toString(): String {
        return "$TAG (type=$type, viewerId=$viewerId action=$action, contentType=$contentType, contentId=$contentId, meta=${meta.toString()})"
    }

    companion object {
        private val TAG = OddMetric::class.java.simpleName
        private val TYPE = "event"

        val ACTION_APP_INIT = "app:init"
        val ACTION_VIEW_LOAD = "view:load"
        val ACTION_VIDEO_PLAY = "video:play"
        val ACTION_VIDEO_PLAYING = "video:playing"
        val ACTION_VIDEO_STOP = "video:stop"
        val ACTION_VIDEO_ERROR = "video:error"
        val ACTION_VIDEO_LOAD = "video:load"
        val ACTION_USER_NEW = "user:new"
    }
}