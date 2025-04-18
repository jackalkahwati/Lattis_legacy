package io.lattis.data.net.ticket

import io.lattis.data.net.base.BaseHttpClient
import io.lattis.data.repository.datasources.api.TicketApi
import io.lattis.data.repository.datasources.api.VehicleApi
import io.lattis.operator.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class TicketApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: TicketApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(TicketApi::class.java)
}