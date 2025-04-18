package com.lattis.data.repository.datasources.api.axa

import com.lattis.data.entity.body.axa.GetKeyFromCloudIdBody
import com.lattis.data.entity.response.axa.GetCloudIdResponse
import com.lattis.data.entity.response.axa.GetKeyResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface AxaLockApi {


    @Headers(
        "X-Api-Key: 79eb4d57e0714d95a590d04479490b42",
        "User-Agent: KSC-TBT; gzip",
        "Content-Type: application/json"
    )
    @GET("api/v1/locks")
    fun getCloudIdFromLockId(@Query("lock_uids") axaLockId:String): Observable<GetCloudIdResponse>


    @Headers(
        "User-Agent: KSC-TBT; gzip",
        "Content-Type: application/json",
        "X-Api-Key: 79eb4d57e0714d95a590d04479490b42"
    )
    @PUT("api/v1/locks/{axaLockCloudId}/slots/1")
    fun getKeyFromCloudId(@Body getKeyFromCloudIdBody: GetKeyFromCloudIdBody, @Path("axaLockCloudId") axaLockId:String): Observable<GetKeyResponse>
}