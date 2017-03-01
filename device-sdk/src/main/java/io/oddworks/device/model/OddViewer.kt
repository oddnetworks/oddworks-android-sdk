package io.oddworks.device.model

import android.os.Parcel
import android.os.Parcelable
import io.oddworks.device.exception.OddResourceException
import io.oddworks.device.model.common.OddRelationship
import io.oddworks.device.model.common.OddResource
import io.oddworks.device.model.common.OddResourceType
import io.oddworks.device.model.common.createParcel
import org.json.JSONObject

class OddViewer(id: String,
                type: OddResourceType,
                relationships: Set<OddRelationship>,
                included: MutableSet<OddResource>,
                meta: JSONObject?,
                val email: String,
                val entitlements: Set<String>,
                val jwt: String): OddResource(id, type, relationships, included, meta), Parcelable {

    protected constructor(source: Parcel): this(
            source.readString(),
            OddResourceType.VIEWER,
            emptySet(),
            mutableSetOf<OddResource>(),
            null,
            source.readString(),
            mutableListOf<String>().apply {
                source.readStringList(this)
            }.toSet(),
            source.readString()
    )


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(email)
        dest.writeStringList(entitlements.toList())
        dest.writeString(jwt)
    }

    override fun describeContents() = 0

    init {
        if (type != OddResourceType.VIEWER) {
            throw OddResourceException("Mismatched OddResourceType: $type")
        }
    }

    fun isEntitledBy(entitlement: String): Boolean {
        return entitlements.contains(entitlement)
    }

    override fun toString(): String {
        return "OddViewer(id: $id, email: $email, entitlements: $entitlements, jwt: ${jwt.subSequence(0, 10)}...)"
    }

    object RELATIONSHIPS {
        @JvmField val WATCHLIST = "watchlist"
    }

    companion object {
        @JvmField
        @Suppress("unused")
        val CREATOR = createParcel(::OddViewer)
    }
}
