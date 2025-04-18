package com.lattis.lattis.data.network.base

enum class ApiEndpoints private constructor(val serverName: String, val url: String) {

    STAGING("Staging", "http://lattisapp-development.lattisapi.io/"),
    PRODUCTION("Production", "https://lattisappv2.lattisapi.io/");

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
