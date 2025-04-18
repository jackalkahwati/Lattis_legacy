package com.lattis.lattis.infrastructure.di.module

import com.lattis.data.mapper.LocationMapper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MapperModule {

    @Provides
    @Singleton
    fun provideLocationMapper(): LocationMapper {
        return LocationMapper()
    }

}