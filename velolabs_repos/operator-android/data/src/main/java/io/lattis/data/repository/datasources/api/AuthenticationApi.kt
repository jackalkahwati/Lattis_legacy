package io.lattis.data.repository.datasources.api


import io.lattis.data.entity.body.authentication.*
import io.lattis.data.entity.response.authentication.AuthenticationResponse
import io.lattis.domain.models.User
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationApi {


    @POST("operator/login")
    fun signIn(@Body body: SignInRequestBody): Observable<User>


}
