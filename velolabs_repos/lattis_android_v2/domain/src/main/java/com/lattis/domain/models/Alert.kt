package com.lattis.domain.models

enum class Alert {

    OFF, THEFT, CRASH;

    var lockId: String? = null

    companion object {

        fun forValue(value: String): Alert? {
            for (alert in values()) {
                if (alert.name == value) {
                    return alert
                }
            }
            return null
        }
    }
}
