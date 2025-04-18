package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import com.lattis.lattis.infrastructure.Lattis
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.lattis.lattis.BuildConfig
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule() {
   @Provides
   @Singleton
   fun provideContext(application: Lattis):Context{
      return application
   }

   @Provides
   @Singleton
   @Named("User-Agent")
   fun provideUserAgent(context: Context): String {
      return when (BuildConfig.FLAVOR_product){
         "lattis"->{ "lattis"}
         "guestbike"->{ "guestbike"}
         "velotransit" -> { "velo_transit"}
         "sandypedals" -> { "sandy_pedals"}
         "giraff" -> { "giraff"}
         "goscoot" -> { "goscoot"}
         "grin" -> {"grin"}
         "grinsantiago" -> {"grin-santiago"}
         "wave" -> {"wave"}
         "wawe" -> {"wawe"}
         "mount" -> {"mount"}
         "unlimitedbiking" -> {"unlimited-biking"}
         "monkeydonkey" -> {"monkey-donkey"}
         "bandwagon" -> {"bandwagon"}
         "ourbike" -> {"ourbike"}
         "fin" -> {"fin"}
         "hooba" -> {"hooba"}
         "blade" -> {"blade"}
         "pacific" -> {"pacificrides"}
         "trip" -> {"trip"}
         "greenriders" -> {"greenriders"}
         "twowheelrental" -> {"twowheelrental"}
         "rockvelo" -> {"rockvelo"}
         "falcosmart" -> {"falcosmart"}
         "thriveryde" -> {"thriveryde"}
         "lockem" -> {"lockem"}
         "robyn" ->{"robyn"}
         "yeti" -> {"yeti"}
         "overwatt" -> {"overwatt"}
         "wbs" -> {"wbs"}
         else ->{"lattis"}
      }
   }


   @Provides
   @Singleton
   @Named("Force-Payment-Method")
   fun provideForcePaymentMethod(): String? {
      return when (BuildConfig.FLAVOR_product){
         "grin" -> {"mercadopago"}
         "grinsantiago" -> {"mercadopago"}
         else ->{null}
      }
   }

   @Provides
   @Singleton
   @Named("Force-Payment-Fleet-Id")
   fun provideForcePaymentFleetId(): Int? {
      return when (BuildConfig.FLAVOR_product){
         "grin" -> {260}
         "grinsantiago" -> {263}
         else ->{null}
      }
   }
}