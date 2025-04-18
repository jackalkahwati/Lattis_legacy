package com.lattis.ellipse.presentation.dagger.module;

import com.lattis.ellipse.presentation.dagger.qualifier.BikeNumberForSearch;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationConfig {

    private static final Integer NEAREST_BIKE_NUMBER_SEARCH = 20;

    @Provides
    @Singleton
    @BikeNumberForSearch
    Integer provideBikeNumberForSearch(){
        return NEAREST_BIKE_NUMBER_SEARCH;
    }


}