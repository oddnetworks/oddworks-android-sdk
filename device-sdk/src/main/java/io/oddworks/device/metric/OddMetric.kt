package io.oddworks.device.metric

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.support.v4.content.ContentResolverCompat
import android.util.Log
import io.oddworks.device.service.OddRxBus
import org.json.JSONException
import org.json.JSONObject

abstract class OddMetric(protected val context: Context, protected val contentType: String?, protected val contentId: String?, protected val sessionId: String, protected val meta: JSONObject?) : OddRxBus.OddRxBusEvent {
    abstract val action: String
    abstract val enabled: Boolean

    val viewerId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    val type: String = "event"

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
            attributes.put("sessionId", sessionId)

            data.put("attributes", attributes)
            data.put("meta", meta)
            json.put("data", data);
        } catch (e: JSONException) {
            Log.d(OddMetric::class.java.simpleName, e.toString())
        }

        return json
    }

    override fun toString(): String {
        return "${OddMetric::class.java.simpleName} (type=$type, viewerId=$viewerId action=$action, contentType=$contentType, contentId=$contentId, meta=${meta.toString()})"
    }

    enum class Type(var key: String, var action: String, var enabled: Boolean, var interval: Int = -1) {
        APP_INIT("appInit", "app:init", true),
        VIEW_LOAD("viewLoad", "view:load", true),
        VIDEO_PLAY("videoPlay", "video:play", true),
        VIDEO_PLAYING("videoPlaying", "video:playing", false, 10000),
        VIDEO_STOP("videoStop", "video:stop", true),
        VIDEO_ERROR("videoError", "video:error", true),
        VIDEO_LOAD("videoLoad", "video:load", true),
        USER_NEW("userNew", "user:new", true)
    }
}