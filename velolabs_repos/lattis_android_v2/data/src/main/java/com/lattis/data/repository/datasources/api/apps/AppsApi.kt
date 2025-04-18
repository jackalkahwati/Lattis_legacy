package com.lattis.data.repository.datasources.api.apps

import com.lattis.data.entity.response.apps.HelpResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

interface AppsApi {
    @GET("api/apps/info")
    fun getHelpInfo(): Observable<HelpResponse>
}