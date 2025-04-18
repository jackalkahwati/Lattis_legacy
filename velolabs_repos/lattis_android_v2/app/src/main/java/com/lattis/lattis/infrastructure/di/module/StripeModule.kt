package com.lattis.lattis.infrastructure.di.module

import android.content.Context
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import dagger.Module
import dagger.Provides
import io.lattis.lattis.R
import javax.inject.Named
import javax.inject.Singleton

@Module
class StripeModule {
    @Provides
    @Singleton
    @Named("StripeKey")
    fun provideStripeKey(context: Context): String {
        return context.getString(R.string.stripe_key)
    }

    @Provides
    @Singleton
    fun provideStripeObject(
        @Named("StripeKey") stripeKey: String,
        context: Context
    ): Stripe {
        PaymentConfiguration.init(context,stripeKey)

        return Stripe(
            context,
            PaymentConfiguration.getInstance(context).publishableKey
        )
    }
}