package com.lattis.data.net.lock

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.lock.LockApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class LockApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: LockApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(LockApi::class.java)

}