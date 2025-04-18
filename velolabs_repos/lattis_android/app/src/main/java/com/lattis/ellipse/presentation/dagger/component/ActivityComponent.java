package com.lattis.ellipse.presentation.dagger.component;

import com.lattis.ellipse.presentation.dagger.module.RepositoryModule;
import com.lattis.ellipse.presentation.ui.home.HomeActivity;

import javax.inject.Singleton;

import dagger.Subcomponent;

@Singleton
@Subcomponent(modules =  RepositoryModule.class)
public interface ActivityComponent {

    void inject(HomeActivity homeActivity);
}
