package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import io.lattis.ellipse.sdk.manager.EllipseManager
import io.lattis.ellipse.sdk.manager.IEllipseManager
import org.jetbrains.annotations.NotNull
import javax.inject.Singleton

@Module
class BluetoothModule {

    @Provides
    @Singleton
    fun provideEllipseManager(context:Context): IEllipseManager {
        return EllipseManager.newInstance(context)
    }
}