package com.lattis.data.net.card

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.bike.BikeApi
import com.lattis.data.repository.datasources.api.card.CardApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.http.Body
import retrofit2.http.POST
import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject


class CardApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: CardApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(CardApi::class.java)

}
