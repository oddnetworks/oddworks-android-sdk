package io.oddworks.device.model

import io.oddworks.device.exception.OddResourceException
import io.oddworks.device.model.common.*
import org.json.JSONObject

class OddView(id: String,
              type: OddResourceType,
              relationships: MutableSet<OddRelationship>,
              included: MutableSet<OddResource>,
              meta: JSONObject?,
              val title: String,
              override val images: Set<OddImage>) : OddResource(id, type, relationships, included, meta), OddImageable {
    init {
        if (type != OddResourceType.VIEW) {
            throw OddResourceException("Mismatched OddResourceType: $type")
        }
    }
}
