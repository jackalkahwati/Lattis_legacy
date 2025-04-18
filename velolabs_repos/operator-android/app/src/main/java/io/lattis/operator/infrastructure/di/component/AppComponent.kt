package io.lattis.operator.infrastructure.di.component

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import io.lattis.operator.infrastructure.Operator
import io.lattis.operator.infrastructure.di.module.*
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityModule::class,
    FragmentModule::class,
    RepositoryModule::class,
    NetworkModule::class,
    AuthenticationModule::class,
    ApplicationModule::class,
    MapperModule::class,
    ApiKeyModule::class
])
interface AppComponent : AndroidInjector<DaggerApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Operator): Builder

        fun build(): AppComponent
    }

    fun inject(application: Operator)
}