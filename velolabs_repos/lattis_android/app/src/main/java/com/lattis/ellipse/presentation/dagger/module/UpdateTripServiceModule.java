package com.lattis.ellipse.presentation.dagger.module;

import android.content.Context;

import com.lattis.ellipse.presentation.ui.ride.service.util.ServiceAction;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ssd3 on 7/28/17.
 */
@Module
public class UpdateTripServiceModule {

    Context context;

    public UpdateTripServiceModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    ServiceAction provideServiceAction(){
        return ServiceAction.newInstance(context);
    }
}
