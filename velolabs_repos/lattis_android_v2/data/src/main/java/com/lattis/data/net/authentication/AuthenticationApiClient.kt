package com.lattis.data.net.authentication

import android.accounts.AccountManager
import com.lattis.data.net.base.BaseAuthHttpClient
import com.lattis.data.repository.datasources.api.authentication.AuthenticationApi
import com.lattis.lattis.data.network.base.ApiEndpoints
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named

class AuthenticationApiClient @Inject constructor(
    val baseAuthHttpClient: BaseAuthHttpClient,
    apiEndpoints: ApiEndpoints
){
    val api: AuthenticationApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseAuthHttpClient.getAuthOkHttpClient())
        .build()
        .create(AuthenticationApi::class.java)
}