package com.lattis.data.net.sasorpslock


import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.net.base.GenericHttpClient
import com.lattis.data.repository.datasources.api.bike.BikeApi
import com.lattis.data.repository.datasources.api.sasorpslock.SaSorPSLockApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class SaSOrPSLockApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: SaSorPSLockApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(SaSorPSLockApi::class.java)

}