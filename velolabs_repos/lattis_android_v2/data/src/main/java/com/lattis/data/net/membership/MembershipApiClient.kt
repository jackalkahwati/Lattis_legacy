package com.lattis.data.net.membership

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.maintenance.MaintenanceApi
import com.lattis.data.repository.datasources.api.membership.MembershipApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject


class MembershipApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: MembershipApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(MembershipApi::class.java)

}