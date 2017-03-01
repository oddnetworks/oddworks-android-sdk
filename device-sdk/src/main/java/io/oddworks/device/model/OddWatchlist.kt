package io.oddworks.device.model

import io.oddworks.device.model.common.OddResource
import org.json.JSONObject

data class OddWatchlist(val viewerId: String, val resource: OddResource, val addToWatchlist: Boolean) {

    fun toJSONObject(): JSONObject {
        return resource.toRelationshipJSONObject()
    }
}
