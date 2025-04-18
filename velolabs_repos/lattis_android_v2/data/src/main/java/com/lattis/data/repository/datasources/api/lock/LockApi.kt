package com.lattis.data.repository.datasources.api.lock

import com.lattis.data.entity.body.lock.SignedMessagePublicKeyBody
import com.lattis.data.entity.response.lock.SignedMessagePublicKeyResponse
import com.lattis.data.entity.response.lock.TapkeyAccessResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface LockApi {
    @POST("api/locks/signed-message-and-public-key-for-trip")
    fun getSignedMessagePublicKey(@Body signedMessagePublicKeyBody: SignedMessagePublicKeyBody): Observable<SignedMessagePublicKeyResponse>


    @GET("api/tapkey/credentials/{mac_id}")
    fun getTapkeyAccess(@Path("mac_id") mac_id:String,@Query("fleetId") fleetId:Int): Observable<TapkeyAccessResponse>
}