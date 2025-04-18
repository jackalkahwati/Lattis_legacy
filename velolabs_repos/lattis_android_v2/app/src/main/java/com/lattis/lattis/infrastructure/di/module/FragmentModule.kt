package com.lattis.lattis.infrastructure.di.module

import com.lattis.lattis.infrastructure.di.scope.ViewScope
import com.lattis.lattis.presentation.bikelist.BikeListFragment
import com.lattis.lattis.presentation.ride.BikeBookedOrActiveRideFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {
    @ViewScope
    @ContributesAndroidInjector
    abstract fun bikeListFragmentInjector():BikeListFragment

    @ViewScope
    @ContributesAndroidInjector
    abstract fun BikeBookedOrActiveRideFragmentInjector(): BikeBookedOrActiveRideFragment
}