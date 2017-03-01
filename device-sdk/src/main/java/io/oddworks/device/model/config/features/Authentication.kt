package io.oddworks.device.model.config.features

data class Authentication(val enabled: Boolean, val type: AuthenticationType, val properties: Map<String, String> = emptyMap()) {
    enum class AuthenticationType(val propertyKeys: Set<String>) {
        LINK(setOf(Authentication.LINK_PROPERTY_URL)),
        DISABLED(emptySet())
    }

    companion object {
        private val LINK_PROPERTY_URL = "url"
    }
}