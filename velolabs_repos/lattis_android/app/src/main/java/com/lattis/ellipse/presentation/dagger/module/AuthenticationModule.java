package com.lattis.ellipse.presentation.dagger.module;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.lattis.ellipse.authentication.EllipseAuthenticator;
import com.lattis.ellipse.data.network.api.AuthenticationApi;
import com.lattis.ellipse.data.network.base.ApiEndpoints;
import com.lattis.ellipse.data.network.model.body.authentication.RefreshTokenBody;
import com.lattis.ellipse.data.network.model.mapper.AccountMapper;
import com.lattis.ellipse.data.network.model.mapper.TermsAndConditionsMapper;
import com.lattis.ellipse.data.network.model.mapper.UserMapper;
import com.lattis.ellipse.data.network.model.response.RefreshTokenResponse;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.presentation.dagger.qualifier.AccountType;
import com.lattis.ellipse.presentation.dagger.qualifier.AuthOkHttpClient;
import com.lattis.ellipse.presentation.dagger.qualifier.AuthenticationTokenType;
import com.lattis.ellipse.presentation.dagger.qualifier.FCMInstanceIdObservable;
import com.lattis.ellipse.presentation.dagger.qualifier.ISO31662Code;
import com.lattis.ellipse.presentation.dagger.qualifier.Refresh412Interceptor;
import com.lattis.ellipse.presentation.dagger.qualifier.UserId;
import com.lattis.ellipse.presentation.dagger.qualifier.UserType;
import com.lattis.ellipse.presentation.dagger.qualifier.UsersId;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lattis.ellipse.R;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


@Module
public class AuthenticationModule {

    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";
    public static final String USER_DATA_USER_ID_KEY  = "USER_DATA_USER_ID_KEY";
    public static final String USER_DATA_USERS_ID_KEY  = "USER_DATA_USERS_ID_KEY";
    public static final String USER_DATA_USER_TYPE_KEY  = "USER_DATA_USER_TYPE_KEY";
    public static final String USER_DATA_USER_VERIFIED = "USER_DATA_USER_VERIFIED";

    @Provides
    @Singleton
    @AuthenticationTokenType
    String provideAuthenticationTokenType(Context context) {
        return context.getString(R.string.account_authentication_token_type);
    }

    @Provides
    @Singleton
    @AccountType
    String provideAccountType(Context context) {
        return context.getString(R.string.account_type);
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager(Context context) {
        return (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Provides @AuthOkHttpClient
    OkHttpClient provideAuthOkHttpClient(AccountManager accountManager,
                                         @AccountType String accountType,
                                         @AuthenticationTokenType String authenticationTokenType) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(generateHttpLoggingInterceptor());
        return builder.build();
    }

    private HttpLoggingInterceptor generateHttpLoggingInterceptor() {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logger;
    }

    @Provides @Singleton
    AuthenticationApi provideAuthenticationApi(Converter.Factory factory,
                                               @AuthOkHttpClient OkHttpClient okHttpClient,
                                               ApiEndpoints endpoints) {
        return new Retrofit.Builder()
                .baseUrl(endpoints.url)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(factory)
                .build().create(AuthenticationApi.class);
    }

    @Provides
    @Singleton
    Authenticator provideAuthenticator(AuthenticationApi api,
                                       UserMapper userMapper,
                                       AccountMapper accountMapper,
                                       TermsAndConditionsMapper termsAndConditionsMapper,
                                       @ISO31662Code String deviceISO31662Code) {
        return new EllipseAuthenticator(api,userMapper,accountMapper,termsAndConditionsMapper,deviceISO31662Code);
    }

    @Provides
    @SuppressWarnings("MissingPermission")
    Account provideAccount(AccountManager accountManager,
                           @AccountType String accountType) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        if (accounts != null && accounts.length > 0) {
            return accountManager.getAccountsByType(accountType)[0];
        }
        return null;
    }

    @Provides
    @UserId
    @SuppressWarnings("MissingPermission")
    String provideUserId(AccountManager accountManager, @AccountType String accountType) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        if (accounts.length > 0) {
            String userId = accountManager.getUserData(accounts[0],USER_DATA_USER_ID_KEY);
            return userId != null ? userId : "none";
        }
        return "none";
    }

    @Provides
    @UsersId
    @SuppressWarnings("MissingPermission")
    String provideUsersId(AccountManager accountManager, @AccountType String accountType) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        if (accounts.length > 0) {
            String usersId = accountManager.getUserData(accounts[0],USER_DATA_USERS_ID_KEY);
            return usersId != null ? usersId : "none";
        }
        return "none";
    }

    @Provides
    @UserType
    @SuppressWarnings("MissingPermission")
    String provideUserType(AccountManager accountManager, @AccountType String accountType) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        if (accounts.length > 0) {
            String userType = accountManager.getUserData(accounts[0],USER_DATA_USER_TYPE_KEY);
            return userType != null ? userType : "none";
        }
        return "none";
    }

    @Provides
    @Singleton
    @FCMInstanceIdObservable
    Observable<String> FCMToken(Context context){
        return Observable.create(emitter -> {
                /*FirebaseApp.initializeApp(context);
                String fcmToken = FirebaseInstanceId.getInstance().getToken();
                if(fcmToken != null){
                    subscriber.onNext(fcmToken);
                } else {
                    ServiceConnection serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder binder) {
                            subscriber.onNext(FirebaseInstanceId.getInstance().getToken());
                        }
                        @Override
                        public void onServiceDisconnected(ComponentName name) {}
                    };
                    context.bindService(new Intent(context, LollipopBluetoothService.class),
                            serviceConnection, Context.BIND_AUTO_CREATE);
                }*/

        });
    }


    @Provides
    @SuppressWarnings("MissingPermission")
    okhttp3.Authenticator provideOkHttpAuthenticator(final AccountManager accountManager,
                                                     final AuthenticationApi authenticationApi,
                                                     @AuthenticationTokenType String authenticationTokenType,
                                                     @UserId String userId,
                                                     @AccountType String accountType){
        return (route, response) -> {
            Account[] accounts = accountManager.getAccountsByType(accountType);
            if (accounts.length > 0) {
                Account account = accounts[0];
                Response<RefreshTokenResponse> refreshTokenResponse = authenticationApi.refreshToken(new RefreshTokenBody(userId, accountManager.getPassword(account))).execute();
                if (refreshTokenResponse.isSuccessful()) {
                    String expiredToken = accountManager.peekAuthToken(account, authenticationTokenType);
                    accountManager.invalidateAuthToken(accountType, expiredToken);
                    accountManager.setAuthToken(account, authenticationTokenType, refreshTokenResponse.body().getTokenResponse().getRestToken());
                    accountManager.setPassword(account, refreshTokenResponse.body().getTokenResponse().getRefreshToken());
                    return response.request().newBuilder().header(HEADER_KEY_AUTHORIZATION, refreshTokenResponse.body().getTokenResponse().getRestToken()).build();
                }
            }
            return response.request();
        };
    }

    @Provides
    @Refresh412Interceptor
    @SuppressWarnings("MissingPermission")
    Interceptor provideRefresh412Intercepter(final AccountManager accountManager,
                                             final AuthenticationApi authenticationApi,
                                             @AuthenticationTokenType String authenticationTokenType,
                                             @AccountType String accountType) {
        return chain -> {
            Request originalRequest = chain.request();

            okhttp3.Response response = chain.proceed(originalRequest);

            if (response.code() == 412) {
                Account[] accounts = accountManager.getAccountsByType(accountType);
                if (accounts.length > 0) {
                    Account account = accounts[0];
                    String userId = accountManager.getUserData(account, USER_DATA_USER_ID_KEY);
                    retrofit2.Response<RefreshTokenResponse> refreshTokenResponse = authenticationApi.refreshToken(new RefreshTokenBody(userId, accountManager.getPassword(account))).execute();
                    if (refreshTokenResponse.isSuccessful()) {
                        String expiredToken = accountManager.peekAuthToken(account, authenticationTokenType);
                        accountManager.invalidateAuthToken(accountType, expiredToken);
                        accountManager.setAuthToken(account, authenticationTokenType, refreshTokenResponse.body().getTokenResponse().getRestToken());
                        accountManager.setPassword(account, refreshTokenResponse.body().getTokenResponse().getRefreshToken());
                        return chain.proceed(originalRequest.newBuilder().header(HEADER_KEY_AUTHORIZATION, refreshTokenResponse.body().getTokenResponse().getRestToken()).build());
                    }
                }
            }
            return response;
        };
    }

}
