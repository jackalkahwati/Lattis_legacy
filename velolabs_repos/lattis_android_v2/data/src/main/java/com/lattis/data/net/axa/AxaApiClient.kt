package com.lattis.data.net.axa

import com.lattis.data.net.base.GenericHttpClient
import com.lattis.data.repository.datasources.api.axa.AxaLockApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class AxaApiClient @Inject constructor(
    val genericHttpClient: GenericHttpClient
)
    {
        val api: AxaLockApi = Retrofit.Builder()
            .baseUrl("https://dev-dot-keysafe-cloud.appspot.com/")
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(genericHttpClient.getAuthOkHttpClient())
            .build()
            .create(AxaLockApi::class.java)

    }
