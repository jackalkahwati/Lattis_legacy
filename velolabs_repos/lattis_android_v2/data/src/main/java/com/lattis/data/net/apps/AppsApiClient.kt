package com.lattis.data.net.apps

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.apps.AppsApi
import com.lattis.data.repository.datasources.api.membership.MembershipApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named

class AppsApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: AppsApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(AppsApi::class.java)

}