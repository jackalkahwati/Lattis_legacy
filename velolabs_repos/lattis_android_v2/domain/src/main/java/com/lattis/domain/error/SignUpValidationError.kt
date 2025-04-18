package com.lattis.domain.error

import java.util.ArrayList

class SignUpValidationError(status: List<Status>) : Throwable() {

    var status: List<Status> = ArrayList()

    init {
        this.status = status
    }

    enum class Status {
        INVALID_EMAIL,
        INVALID_PASSWORD
    }
}
