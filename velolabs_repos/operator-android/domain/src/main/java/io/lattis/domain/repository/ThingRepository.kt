package io.lattis.domain.repository

import io.lattis.domain.models.ThingStatus
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Path

interface ThingRepository {
    fun getThingStatus(thingId:Int):Observable<ThingStatus>
    fun lockIt(thing_id:Int): Observable<ResponseBody>
    fun unlockIt(thing_id:Int): Observable<ResponseBody>
    fun unconverIt(thing_id:Int): Observable<ResponseBody>
    fun lightItOnOffFlicker( thing_id:Int,headLight:Int?,tailLight:Int?): Observable<ResponseBody>
    fun sound( thing_id:Int,controlType:Int?,workMode:Int?): Observable<Boolean>
}