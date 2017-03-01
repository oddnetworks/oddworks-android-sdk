package io.oddworks.device.request

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.util.*

object SafeJSONParser {

    /**
     * @param json  object containing the String value
     * *
     * @param key   key at which the String value is located
     * *
     * @return      defaults to null
     */
    fun getString(json: JSONObject?, key: String): String? {
        if (json == null) {
            return null
        }
        try {
            if (json.isNull(key)) {
                return null
            }
            return json.getString(key)
        } catch (e: JSONException) {
            return null
        }

    }

    /**
     * @param json  object containing the DateTime value
     * *
     * @param key   key at which the DateTime value is located
     * *
     * @return      defaults to null
     */
    fun getDate(json: JSONObject?, key: String): Date? {
        if (json == null) {
            return null
        }
        if (!json.isNull(key)) {
            try {
                val df = ISO8601DateFormat()
                return df.parse(getString(json, key))
            } catch (exception: ParseException) {
                return null
            }

        }
        return null
    }

    /**
     * @param json  object containing the int value
     * *
     * @param key   key at which the int value is located
     * *
     * @return      defaults to 0
     */
    fun getInt(json: JSONObject?, key: String): Int {
        if (json == null) {
            return 0
        }
        return json.optInt(key, 0)
    }

    /**
     * @param json  object containing the boolean value
     * *
     * @param key   key at which the boolean value is located
     * *
     * @return      defaults to false
     */
    fun getBoolean(json: JSONObject?, key: String): Boolean {
        if (json == null) {
            return false
        }
        return json.optBoolean(key, false)
    }

    @Throws(JSONException::class)
    fun getJSONObject(json: JSONObject?, key: String, throwOnNull: Boolean = false): JSONObject? {
        if (throwOnNull && json == null) {
            throw JSONException("Object is null")
        } else if (json == null) {
            return null
        }
        val jsonObject = json.optJSONObject(key)
        if (throwOnNull && jsonObject == null) {
            throw JSONException("Unable to parse object: " + key)
        }
        return jsonObject
    }

    @Throws(JSONException::class)
    fun getJSONArray(json: JSONObject?, key: String, throwOnNull: Boolean = false): JSONArray? {
        if (throwOnNull && json == null) {
            throw JSONException("Array is null")
        } else if (json == null) {
            return null
        }
        val jsonArray = json.optJSONArray(key)
        if (throwOnNull && jsonArray == null) {
            throw JSONException("Unable to parse array: " + key)
        }
        return jsonArray
    }
}
