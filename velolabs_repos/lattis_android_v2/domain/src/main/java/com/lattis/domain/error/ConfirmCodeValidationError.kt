package com.lattis.domain.error

import java.util.*

class ConfirmCodeValidationError(status: List<Status>) :
    Throwable() {
    var status: List<Status> = ArrayList()

    enum class Status {
        INVALID_CONFIRMATION_CODE
    }

    init {
        this.status = status
    }
}