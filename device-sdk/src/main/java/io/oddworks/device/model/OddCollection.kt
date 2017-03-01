package io.oddworks.device.model

import io.oddworks.device.exception.OddResourceException
import io.oddworks.device.model.common.*
import org.json.JSONObject
import java.util.*

class OddCollection(id: String,
                    type: OddResourceType,
                    relationships: Set<OddRelationship>,
                    included: MutableSet<OddResource>,
                    meta: JSONObject?,
                    val title: String,
                    val description: String,
                    override val images: Set<OddImage>,
                    val genres: Set<String>,
                    val releaseDate: Date?) : OddResource(id, type, relationships, included, meta), OddImageable {

    init {
        if (type != OddResourceType.COLLECTION) {
            throw OddResourceException("Mismatched OddResourceType: $type")
        }
    }

    fun isEntitled(): Boolean {
        if (meta != null) {
            return meta.optBoolean("entitled", false)
        }

        return false
    }

    object RELATIONSHIPS {
        @JvmField val ENTITIES = "entities"
        @JvmField val FEATURED = "featured"
    }
}
