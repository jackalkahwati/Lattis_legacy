package io.lattis.data.net.authentication

import android.accounts.AccountManager
import io.lattis.data.net.base.BaseAuthHttpClient
import io.lattis.data.repository.datasources.api.AuthenticationApi
import io.lattis.operator.data.network.base.ApiEndpoints
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named

class AuthenticationApiClient @Inject constructor(
        val baseAuthHttpClient: BaseAuthHttpClient,
        apiEndpoints: ApiEndpoints
){
    val api: AuthenticationApi = Retrofit.Builder()
        .baseUrl(apiEndpoints.url)   // This is temporary, API URL will be handled depending upon product flavor
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(baseAuthHttpClient.getAuthOkHttpClient())
        .build()
        .create(AuthenticationApi::class.java)
}