package io.lattis.operator.infrastructure.di.module

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.lattis.operator.infrastructure.Operator
import javax.inject.Singleton

@Module
class ApplicationModule() {
   @Provides
   @Singleton
   fun provideContext(application: Operator):Context{
      return application
   }

   @Provides
   @Singleton
   fun provideSharedPreferences(context: Context): SharedPreferences {
      return androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
   }
}