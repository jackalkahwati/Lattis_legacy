package io.lattis.data.repository.datasources.api

import io.lattis.data.entity.body.thing.HeadTailLightBody
import io.lattis.data.entity.body.thing.SoundBody
import io.lattis.domain.models.ThingStatus
import io.lattis.domain.models.Vehicle
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface ThingApi {
    @GET("operator/things/{thing_id}/status")
    fun getThingStatus(@Path("thing_id") thing_id:Int): Observable<ThingStatus>

    @PUT("operator/things/{thing_id}/lock")
    fun lockIt(@Path("thing_id") thing_id:Int): Observable<ResponseBody>

    @PUT("operator/things/{thing_id}/unlock")
    fun unlockIt(@Path("thing_id") thing_id:Int): Observable<ResponseBody>

    @PUT("operator/things/{thing_id}/uncover")
    fun uncoverIt(@Path("thing_id") thing_id:Int): Observable<ResponseBody>

    @PUT("operator/things/{thing_id}/light")
    fun lightItOnOffFlicker(@Path("thing_id") thing_id:Int,@Body headTailLightBody: HeadTailLightBody): Observable<ResponseBody>

    @PUT("operator/things/{thing_id}/sound")
    fun sound(@Path("thing_id") thing_id:Int,@Body soundBody: SoundBody): Observable<ResponseBody>

}