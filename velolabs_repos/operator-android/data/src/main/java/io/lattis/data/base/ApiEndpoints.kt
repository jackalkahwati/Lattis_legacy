package io.lattis.operator.data.network.base

enum class ApiEndpoints private constructor(val serverName: String, val url: String) {

    STAGING("Staging", "https://dev.api.lattis.io/"),
    PRODUCTION("Production", "https://api.lattis.io/");

    override fun toString(): String {
        return serverName
    }

    companion object {

        fun from(endpoint: String): ApiEndpoints? {
            for (value in values()) {
                if (value.url != null && value.url == endpoint) {
                    return value
                }
            }
            return null
        }
    }
}
