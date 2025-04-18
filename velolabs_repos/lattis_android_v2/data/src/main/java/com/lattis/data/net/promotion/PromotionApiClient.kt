package com.lattis.data.net.promotion

import com.lattis.data.net.base.BaseHttpClient
import com.lattis.data.repository.datasources.api.promotion.PromotionApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class PromotionApiClient @Inject constructor(
    baseHttpClient: BaseHttpClient,
    apiEndpoints: ApiEndpoints
) {
    val api: PromotionApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseHttpClient.provideApiOkHttpClient())
        .build()
        .create(PromotionApi::class.java)

}