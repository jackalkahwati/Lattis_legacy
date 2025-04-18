package io.lattis.data.entity.response.authentication

import io.lattis.data.entity.response.base.AbstractApiResponse
import io.lattis.domain.models.User

class AuthenticationResponse : AbstractApiResponse() {
    val user: User? = null
}
