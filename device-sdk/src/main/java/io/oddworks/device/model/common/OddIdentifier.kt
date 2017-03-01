package io.oddworks.device.model.common

open class OddIdentifier(val id: String, val type: OddResourceType) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as OddIdentifier?

        if (id != that!!.id) return false
        return type == that.type

    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
