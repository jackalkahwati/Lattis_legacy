package com.lattis.ellipse.presentation.dagger.module;

import android.content.Context;

import com.lattis.ellipse.data.platform.mapper.LocationMapper;
import com.lattis.ellipse.data.platform.AndroidLocationRepository;
import com.lattis.ellipse.data.platform.mapper.LocationToMapBoxMapper;
import com.lattis.ellipse.domain.repository.LocationRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by raverat on 2/23/17.
 */

@Module
public class LocationModule {

    @Provides
    @Singleton
    public LocationRepository provideLocationRepository(Context context,
                                                        LocationMapper locationMapper) {
        return new AndroidLocationRepository(context, locationMapper);
    }

    @Provides
    @Singleton
    public LocationMapper provideLocationMapper() {
        return new LocationMapper();
    }


    @Provides
    @Singleton
    public LocationToMapBoxMapper provideLocationToMapBoxMapper() {
        return new LocationToMapBoxMapper();
    }



}
