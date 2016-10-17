package io.oddworks.device.model.common

import org.json.JSONObject
import java.util.*

open class OddResource(val identifier: OddIdentifier, var relationships: Set<OddRelationship>, val included: MutableSet<OddResource>, val meta: JSONObject?) {
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
            val found = included.find { it.identifier == identifier }
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
                val included = included.find { it.identifier == identifier }
                if (included == null) {
                    isMissing = true
                }
            }
        }
        return isMissing
    }
}