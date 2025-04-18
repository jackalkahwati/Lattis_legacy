package io.lattis.data.repository.implementation.api

import io.lattis.data.entity.body.thing.HeadTailLightBody
import io.lattis.data.entity.body.thing.SoundBody
import io.lattis.data.net.thing.ThingApiClient
import io.lattis.domain.models.ThingStatus
import io.lattis.domain.repository.ThingRepository
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Path
import javax.inject.Inject

class ThingRepositoryImp @Inject constructor(
    val thingApiClient: ThingApiClient
):ThingRepository {

    override fun getThingStatus(thingId: Int): Observable<ThingStatus> {
        return thingApiClient.api.getThingStatus(thingId)
    }

    override fun lockIt(thing_id:Int): Observable<ResponseBody>{
        return thingApiClient.api.lockIt(thing_id)
    }

    override fun unlockIt(thing_id:Int): Observable<ResponseBody>{
        return thingApiClient.api.unlockIt(thing_id)
    }

    override fun unconverIt(thing_id:Int): Observable<ResponseBody>{
        return thingApiClient.api.uncoverIt(thing_id)
    }


    override fun lightItOnOffFlicker( thing_id:Int,headLight:Int?,tailLight:Int?): Observable<ResponseBody>{
        return thingApiClient.api.lightItOnOffFlicker(thing_id, HeadTailLightBody(headLight,tailLight))
    }

    override fun sound( thing_id:Int,controlType:Int?,workMode:Int?): Observable<Boolean>{
        return thingApiClient.api.sound(thing_id, SoundBody(controlType,workMode)).map {
            true
        }
    }
}