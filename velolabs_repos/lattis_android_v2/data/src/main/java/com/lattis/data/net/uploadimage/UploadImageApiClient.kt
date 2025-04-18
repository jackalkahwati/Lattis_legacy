package com.lattis.data.net.uploadimage

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.ride.RideApi
import com.lattis.data.repository.datasources.api.uploadimage.UploadImageApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class UploadImageApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: UploadImageApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(UploadImageApi::class.java)

}