package io.lattis.operator.infrastructure.di.module

import dagger.Module
import dagger.Provides
import io.lattis.data.mapper.LocationMapper
import javax.inject.Singleton

@Module
class MapperModule {

    @Provides
    @Singleton
    fun provideLocationMapper(): LocationMapper {
        return LocationMapper()
    }

}