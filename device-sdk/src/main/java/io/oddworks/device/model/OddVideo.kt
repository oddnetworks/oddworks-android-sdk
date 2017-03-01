package io.oddworks.device.model


import io.oddworks.device.exception.OddResourceException
import io.oddworks.device.model.common.*
import io.oddworks.device.model.video.OddCast
import io.oddworks.device.model.video.OddSource
import io.oddworks.device.model.video.OddSourceable
import org.json.JSONObject
import java.util.*

class OddVideo(id: String,
               type: OddResourceType,
               relationships: MutableSet<OddRelationship>,
               included: MutableSet<OddResource>,
               meta: JSONObject?,
               val title: String,
               val description: String,
               override val images: Set<OddImage>,
               override val sources: Set<OddSource>,
               val duration: Int = 0,
               val genres: Set<String>,
               val cast: Set<OddCast>,
               val tags: Set<String>,
               val releaseDate: Date?,
               val position: Int = 0,
               val complete: Boolean = false) : OddResource(id, type, relationships, included, meta), OddImageable, OddSourceable {
    init {
        if (type != OddResourceType.VIDEO) {
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
        @JvmField val RELATED = "related"
    }
}
