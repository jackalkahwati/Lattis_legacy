package com.lattis.data.repository.datasources.api.dockhub

import com.lattis.data.entity.body.dockhub.UndockBody
import com.lattis.data.entity.response.BasicResponse
import com.lattis.data.entity.response.card.GetCardResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface DockHubApi {

    @POST("api/hubs/{uuid}/undock")
    fun undock(@Path("uuid") uuid:String, @Body undockBody: UndockBody): Observable<BasicResponse>
}