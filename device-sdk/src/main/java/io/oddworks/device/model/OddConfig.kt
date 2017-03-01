package io.oddworks.device.model

import android.util.Log
import io.oddworks.device.exception.OddResourceException
import io.oddworks.device.model.common.OddResource
import io.oddworks.device.model.common.OddResourceType
import io.oddworks.device.model.config.Display
import io.oddworks.device.model.config.Features
import org.json.JSONObject

class OddConfig(id: String,
                type: OddResourceType,
                meta: JSONObject?,
                val views: Map<String, String>,
                val display: Display,
                val features: Features) : OddResource(id, type, mutableSetOf(), mutableSetOf(), meta){

    init {
        if (type != OddResourceType.CONFIG) {
            throw OddResourceException("Mismatched OddResourceType: $type")
        }

        Log.d(OddConfig::class.java.simpleName, "views[${views.keys}]")
    }
}
