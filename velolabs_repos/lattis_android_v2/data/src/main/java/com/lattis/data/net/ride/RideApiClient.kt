package com.lattis.data.net.ride

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.bike.BikeApi
import com.lattis.data.repository.datasources.api.ride.RideApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import com.lattis.domain.models.Ride
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class RideApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: RideApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(RideApi::class.java)

}