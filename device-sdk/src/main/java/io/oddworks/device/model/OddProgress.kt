package io.oddworks.device.model

import org.json.JSONObject

data class OddProgress(val videoId: String, val position: Int, val complete: Boolean) {
    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        val data = JSONObject()

        data.put("type", "progress")

        data.put("position", position)
        data.put("complete", complete)

        json.put("data", data)

        return json
    }
}
