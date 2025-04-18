package com.lattis.lattis.infrastructure.di.component

import com.lattis.lattis.infrastructure.Lattis
import com.lattis.lattis.infrastructure.di.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityModule::class,
    FragmentModule::class,
    ServiceModule::class,
    RepositoryModule::class,
    AuthenticationModule::class,
    ApplicationModule::class,
    ApiKeyModule::class,
    MapperModule::class,
    DeviceModule::class,
    NetworkModule::class,
    RealmModule::class,
    BluetoothModule::class,
    SettingsModule::class,
    StripeModule::class,
    HelperModule::class
])
interface AppComponent : AndroidInjector<DaggerApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Lattis): Builder

        fun build(): AppComponent
    }

    fun inject(application: Lattis)
}