package com.lattis.ellipse.presentation.dagger.module;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.lattis.ellipse.data.network.api.AlertApi;
import com.lattis.ellipse.data.network.api.AuthenticationApi;
import com.lattis.ellipse.data.network.api.BikeApi;
import com.lattis.ellipse.data.network.api.CardApi;
import com.lattis.ellipse.data.network.api.LockApi;
import com.lattis.ellipse.data.network.api.MaintenanceApi;
import com.lattis.ellipse.data.network.api.ParkingApi;
import com.lattis.ellipse.data.network.api.RideApi;
import com.lattis.ellipse.data.network.api.UploadImageApi;
import com.lattis.ellipse.data.network.api.UserApi;
import com.lattis.ellipse.data.network.base.ApiEndpoints;
import com.lattis.ellipse.mock.MockLockApi;
import com.lattis.ellipse.mock.MockUserApi;
import com.lattis.ellipse.presentation.dagger.qualifier.AccountType;
import com.lattis.ellipse.presentation.dagger.qualifier.ApiOkHttpClient;
import com.lattis.ellipse.presentation.dagger.qualifier.AuthenticationTokenType;
import com.lattis.ellipse.presentation.dagger.qualifier.Refresh412Interceptor;
import com.lattis.ellipse.presentation.dagger.qualifier.UserId;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lattis.ellipse.BuildConfig;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;

@Module
public class NetworkModule {

    private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";
    private static final String HEADER_VALUE_CONTENT_TYPE = "application/json";

    @Provides @Singleton
    Converter.Factory provideGsonConverter() {
        return GsonConverterFactory.create();
    }

    @Provides
    @ApiOkHttpClient
    OkHttpClient provideApiOkHttpClient(AccountManager accountManager,
                                        AuthenticationApi authenticationApi,
                                        @AccountType String accountType,
                                        @AuthenticationTokenType String authenticationTokenType,
                                        @UserId String userId,
                                        @Refresh412Interceptor Interceptor refresh412Interceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(generateHttpLoggingInterceptor());
        builder.addInterceptor(generateClientHeaderInterceptor(accountManager,accountType,authenticationTokenType));
        builder.addInterceptor(refresh412Interceptor);
        return builder.build();
    }

    private HttpLoggingInterceptor generateHttpLoggingInterceptor() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logger;
    }

    @SuppressWarnings("MissingPermission")
    private Interceptor generateClientHeaderInterceptor(AccountManager accountManager,
                                                        @AccountType String accountType,
                                                        @AuthenticationTokenType String authenticationTokenType) {
        return chain -> {
            Account[] accounts = accountManager.getAccountsByType(accountType);
            Request.Builder builder = chain.request().newBuilder();
            builder.header(HEADER_KEY_CONTENT_TYPE, HEADER_VALUE_CONTENT_TYPE);
            if (accounts.length > 0) {
                String token = accountManager.peekAuthToken(accounts[0],authenticationTokenType);
                Log.e("NetworkModule","Token is "+token);

                if(token != null){
                    builder.header(HEADER_KEY_AUTHORIZATION,token);
                }
            }
            return chain.proceed(builder.build());
        };
    }

    @Provides @Singleton
    UserApi provideUserApi(Context context,
                           Converter.Factory factory,
                           @ApiOkHttpClient OkHttpClient okHttpClient,
                           ApiEndpoints apiEndpoints) {
        if(BuildConfig.USE_MOCK){
            NetworkBehavior behavior = NetworkBehavior.create();
            behavior.setDelay(1, TimeUnit.SECONDS);
            behavior.setFailurePercent(0);
            OkHttpClient mockClient = new OkHttpClient.Builder().build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiEndpoints.url)
                    .client(mockClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(factory)
                    .build();
            return new MockUserApi(context,new MockRetrofit.Builder(retrofit).networkBehavior(behavior).build().create(UserApi.class));
        } else {
            return new Retrofit.Builder()
                    .baseUrl(apiEndpoints.url)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(factory)
                    .build().create(UserApi.class);
        }
    }

    @Provides @Singleton
    LockApi provideLockApi(Context context,
                           Converter.Factory factory,
                           @ApiOkHttpClient OkHttpClient okHttpClient,
                           ApiEndpoints apiEndpoints) {
        if(BuildConfig.USE_MOCK){
            NetworkBehavior behavior = NetworkBehavior.create();
            behavior.setDelay(1, TimeUnit.SECONDS);
            behavior.setFailurePercent(0);
            OkHttpClient mockClient = new OkHttpClient.Builder().build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiEndpoints.url)
                    .client(mockClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(factory)
                    .build();
            return new MockLockApi(context, new MockRetrofit.Builder(retrofit).networkBehavior(behavior).build().create(LockApi.class));
        } else {
            return new Retrofit.Builder()
                    .baseUrl(apiEndpoints.url)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(factory)
                    .build().create(LockApi.class);
        }
    }

    @Provides @Singleton
    AlertApi provideAlertApi(Converter.Factory factory,
                             @ApiOkHttpClient OkHttpClient okHttpClient,
                            ApiEndpoints apiEndpoints) {
        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(AlertApi.class);
    }



    @Provides @Singleton
    BikeApi provideBikeApi(Converter.Factory factory,
                           @ApiOkHttpClient OkHttpClient okHttpClient,
                           ApiEndpoints apiEndpoints) {

        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(BikeApi.class);
    }

    @Provides @Singleton
    ParkingApi provideParkingApi(Converter.Factory factory,
                                 @ApiOkHttpClient OkHttpClient okHttpClient,
                                 ApiEndpoints apiEndpoints) {

        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(ParkingApi.class);
    }

    @Provides @Singleton
    RideApi provideTripApi(Converter.Factory factory,
                           @ApiOkHttpClient OkHttpClient okHttpClient,
                           ApiEndpoints apiEndpoints) {

        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory
                        .createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(RideApi.class);
    }

    @Provides @Singleton
    UploadImageApi provideUploadImageApi(Converter.Factory factory,
                                         @ApiOkHttpClient OkHttpClient okHttpClient,
                                           ApiEndpoints apiEndpoints){

        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory
                        .createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(UploadImageApi.class);
    }

    @Provides @Singleton
    MaintenanceApi provideMaintenanceApi(Converter.Factory factory,
                                         @ApiOkHttpClient OkHttpClient okHttpClient,
                                         ApiEndpoints apiEndpoints){

        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory
                        .createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(MaintenanceApi.class);
    }


    @Provides @Singleton
    CardApi provideCardApi(Converter.Factory factory,
                                         @ApiOkHttpClient OkHttpClient okHttpClient,
                                         ApiEndpoints apiEndpoints){

        return new Retrofit.Builder()
                .baseUrl(apiEndpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory
                        .createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(CardApi.class);
    }

}
