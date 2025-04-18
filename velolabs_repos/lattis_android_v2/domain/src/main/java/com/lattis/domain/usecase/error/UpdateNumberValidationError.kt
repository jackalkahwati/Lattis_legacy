package com.lattis.domain.usecase.error

import java.util.*

class UpdateNumberValidationError(status: List<Status>) :
    Throwable() {
    var status: List<Status> =
        ArrayList()

    enum class Status {
        INVALID_PHONE_NUMBER
    }

    init {
        this.status = status
    }
}