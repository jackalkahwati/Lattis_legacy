package com.lattis.ellipse.presentation.dagger.module;

import android.content.Context;

import com.lattis.ellipse.presentation.dagger.qualifier.DatabaseName;
import com.lattis.ellipse.presentation.dagger.qualifier.StripeKey;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lattis.ellipse.R;

@Module
public class StripeModule {

    @Provides
    @Singleton
    @StripeKey
    String provideStripeKey(Context context) {
        return context.getString(R.string.stripe_key);
    }

    @Provides
    @Singleton
    Stripe provideStripeObject(@StripeKey String stripeKey, Context context) {
        PaymentConfiguration.init(stripeKey);
        return new Stripe(context,
                PaymentConfiguration.getInstance().getPublishableKey());
    }


}
