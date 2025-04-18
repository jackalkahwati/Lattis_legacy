package io.lattis.operator.infrastructure.di.module

import dagger.Module
import dagger.Provides
import io.lattis.operator.BuildConfig
import io.lattis.operator.data.network.base.ApiEndpoints
import javax.inject.Singleton


@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideApiEndpoints(): ApiEndpoints {
        return if (BuildConfig.FLAVOR.equals("operatorBeta", true)) {
            ApiEndpoints.PRODUCTION
        } else if (BuildConfig.FLAVOR.equals("operatorDev", true)) {
            ApiEndpoints.STAGING
        } else {
            return ApiEndpoints.PRODUCTION
        }
    }
}