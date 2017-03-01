package io.oddworks.device.model.common

import org.json.JSONObject
import java.util.*

open class OddResource(id: String, type: OddResourceType, var relationships: Set<OddRelationship>, val included: MutableSet<OddResource>, val meta: JSONObject?): OddIdentifier(id, type) {
    fun getRelationship(name: String): OddRelationship? {
        return relationships.find {
            it.name == name
        }
    }

    fun getIdentifiersByRelationship(name: String): LinkedHashSet<OddIdentifier> {
        val relationship = getRelationship(name) ?: return linkedSetOf()

        return relationship.identifiers
    }

    fun getIncludedByRelationship(name: String): LinkedHashSet<OddResource> {
        val relationship = getRelationship(name) ?: return linkedSetOf()

        val inc = linkedSetOf<OddResource>()

        relationship.identifiers.forEach { identifier ->
            val found = included.find { it.id == identifier.id && it.type == identifier.type }
            if (found != null) {
                inc.add(found)
            }
        }

        return inc
    }

    fun isMissingIncluded(): Boolean {
        var isMissing = false
        relationships.forEach {
            it.identifiers.forEach { identifier ->
                val included = included.find { it.id == identifier.id && it.type == identifier.type }
                if (included == null) {
                    isMissing = true
                }
            }
        }
        return isMissing
    }

    fun toRelationshipJSONObject(): JSONObject {
        val json = JSONObject()
        val data = JSONObject()
        data.put("id", id)
        data.put("type", type.toString().toLowerCase())
        json.put("data", data)
        return json
    }
}