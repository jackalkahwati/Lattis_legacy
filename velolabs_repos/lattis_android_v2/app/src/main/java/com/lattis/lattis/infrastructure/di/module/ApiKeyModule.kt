package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import io.lattis.lattis.R
import javax.inject.Named

@Module
class ApiKeyModule {


    @Provides
    @Named("GoogleApiKey")
    fun provideGoogleApiKey(context: Context):String{
        return context.getString(R.string.google_maps_key)
    }
}