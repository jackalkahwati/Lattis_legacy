package io.lattis.data.repository.datasources.api

import io.lattis.domain.models.Fleet
import io.lattis.domain.models.User
import io.reactivex.Observable
import retrofit2.http.GET

interface UserApi {
    @GET("operator/me")
    fun getMe(): Observable<User.Operator>
}