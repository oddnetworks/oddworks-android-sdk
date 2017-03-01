package io.oddworks.device

import android.content.Context
import android.util.Log
import io.oddworks.device.model.OddViewer
import io.oddworks.device.model.common.OddRelationship
import io.oddworks.device.model.common.OddResourceType

object Oddworks {
    @JvmField val DEFAULT_API_BASE_URL = "https://content.oddworks.io/v2"
    @JvmField val DEFAULT_ANALYTICS_API_BASE_URL = "https://analytics.oddworks.io"

    @JvmField val CONFIG_JWT_KEY = "io.oddworks.configJWT"
    @JvmField val API_BASE_URL_KEY = "io.oddworks.apiBaseURL"
    @JvmField val ANALYTICS_API_BASE_URL_KEY = "io.oddworks.analyticsApiBaseURL"

    private val AUTHORIZED_VIEWER_PREFERENCES = "${Oddworks::class.java.name}.AUTHORIZED_VIEWER_PREFERENCES"
    private val AUTHORIZED_VIEWER_ID = "${Oddworks::class.java.name}.AUTHORIZED_VIEWER_ID"
    private val AUTHORIZED_VIEWER_EMAIL = "${Oddworks::class.java.name}.AUTHORIZED_VIEWER_EMAIL"
    private val AUTHORIZED_VIEWER_ENTITLEMENTS = "${Oddworks::class.java.name}.AUTHORIZED_VIEWER_ENTITLEMENTS"
    private val AUTHORIZED_VIEWER_JWT = "${Oddworks::class.java.name}.AUTHORIZED_VIEWER_JWT"

    @JvmStatic fun getStoredViewer(ctx: Context): OddViewer? {
        val prefs = ctx.getSharedPreferences(AUTHORIZED_VIEWER_PREFERENCES, Context.MODE_PRIVATE)
        val id = prefs.getString(AUTHORIZED_VIEWER_ID, null)
        val email = prefs.getString(AUTHORIZED_VIEWER_EMAIL, null)
        val entitlements = prefs.getStringSet(AUTHORIZED_VIEWER_ENTITLEMENTS, emptySet())
        val jwt = prefs.getString(AUTHORIZED_VIEWER_JWT, null)
        try {
            return OddViewer(id, OddResourceType.VIEWER, emptySet<OddRelationship>(), mutableSetOf(), null, email, entitlements, jwt)
        } catch (e: Exception) {
            Log.w(Oddworks::class.java.simpleName, "getViewer failed", e)
            return null
        }
    }

    @JvmStatic fun setStoredViewer(ctx: Context, viewer: OddViewer) {
        val prefs = ctx.getSharedPreferences(AUTHORIZED_VIEWER_PREFERENCES, Context.MODE_PRIVATE).edit()
        prefs.putString(AUTHORIZED_VIEWER_ID, viewer.id)
        prefs.putString(AUTHORIZED_VIEWER_EMAIL, viewer.email)
        prefs.putStringSet(AUTHORIZED_VIEWER_ENTITLEMENTS, viewer.entitlements)
        prefs.putString(AUTHORIZED_VIEWER_JWT, viewer.jwt)
        prefs.apply()
    }

    @JvmStatic fun clearStoredViewer(ctx: Context) {
        val prefs = ctx.getSharedPreferences(AUTHORIZED_VIEWER_PREFERENCES, Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
    }
}
