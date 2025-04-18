package com.lattis.lattis.inject.component

import android.app.Application
import com.lattis.domain.repository.UserRepository
import com.lattis.lattis.infrastructure.di.component.AppComponent
import com.lattis.lattis.infrastructure.di.module.ActivityModule
import com.lattis.lattis.infrastructure.di.module.RepositoryModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityModule::class,
    RepositoryModule::class
])
interface TestAppComponent : AppComponent {
    fun userRepository() : UserRepository
    fun postRepository() : PostRepository

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): TestAppComponent.Builder

        fun build(): TestAppComponent
    }
}