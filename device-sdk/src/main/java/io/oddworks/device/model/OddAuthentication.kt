package io.oddworks.device.model

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

data class OddAuthentication(val email: String, val password: String) {
    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        val attributes = JSONObject()

        try {
            val data = JSONObject()

            data.put("type", "authentication")

            attributes.put("email", email)
            attributes.put("password", password)

            data.put("attributes", attributes)
            json.put("data", data);
        } catch (e: JSONException) {
            Log.d(OddAuthentication::class.java.simpleName, e.toString())
        }

        return json
    }

    companion object {
        val ENDPOINT: String = "login"
    }
}