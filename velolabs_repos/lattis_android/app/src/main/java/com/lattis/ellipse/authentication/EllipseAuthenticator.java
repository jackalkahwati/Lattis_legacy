package com.lattis.ellipse.authentication;

import com.lattis.ellipse.data.network.api.AuthenticationApi;
import com.lattis.ellipse.data.network.model.body.authentication.GetNewTokensBody;
import com.lattis.ellipse.data.network.model.body.authentication.ResetPasswordBody;
import com.lattis.ellipse.data.network.model.body.authentication.SendCodeForForgotPasswordBody;
import com.lattis.ellipse.data.network.model.body.authentication.SendVerificationCodeBody;
import com.lattis.ellipse.data.network.model.body.authentication.SignInRequestBody;
import com.lattis.ellipse.data.network.model.body.authentication.SignUpRequestBody;
import com.lattis.ellipse.data.network.model.body.commun.VerificationCodeBody;
import com.lattis.ellipse.data.network.model.mapper.AccountMapper;
import com.lattis.ellipse.data.network.model.mapper.TermsAndConditionsMapper;
import com.lattis.ellipse.data.network.model.mapper.UserMapper;
import com.lattis.ellipse.data.network.model.response.AuthenticationResponse;
import com.lattis.ellipse.data.network.model.response.ConfirmCodeForForgotPasswordBody;
import com.lattis.ellipse.data.network.model.response.NewTokenResponse;
import com.lattis.ellipse.data.network.model.response.ValidationResponse;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.repository.Authenticator;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class EllipseAuthenticator implements Authenticator {

    private static final String TAG = EllipseAuthenticator.class.getName();
    private AuthenticationApi authenticationApi;
    private UserMapper userMapper;
    private AccountMapper accountMapper;
    private TermsAndConditionsMapper termsAndConditionsMapper;
    private String deviceISO31662Code;

    @Inject
    public EllipseAuthenticator(AuthenticationApi authenticationApi,
                                UserMapper userMapper,
                                AccountMapper accountMapper,
                                TermsAndConditionsMapper termsAndConditionsMapper,
                                String deviceISO31662Code) {
        this.authenticationApi = authenticationApi;
        this.userMapper = userMapper;
        this.accountMapper = accountMapper;
        this.termsAndConditionsMapper = termsAndConditionsMapper;
        this.deviceISO31662Code = deviceISO31662Code;
    }

    @Override
    public Observable<Account> signIn(String userType,
                                                     String usersId,
                                                     String password,
                                                     String fcmToken) {
        return authenticationApi.signIn(
                new SignInRequestBody(
                        userType,
                        usersId,
                        fcmToken,
                        password,
                        false)).flatMap(new Function<AuthenticationResponse, Observable<Account>>() {
                            @Override
                            public Observable<Account> apply(AuthenticationResponse authenticationResponse) {
                                return authenticationApi
                                        .getNewTokens(new GetNewTokensBody(authenticationResponse.getUser().getUserId(), password))
                                        .flatMap(new Function<NewTokenResponse, Observable<Account>>() {
                                            @Override
                                            public Observable<Account> apply(NewTokenResponse newTokenResponse) {
                                                authenticationResponse.getUser().setRestToken(newTokenResponse.getToken().getRestToken());
                                                authenticationResponse.getUser().setRefreshToken(newTokenResponse.getToken().getRefreshToken());
                                                return Observable.just(accountMapper.mapIn(authenticationResponse.getUser()));
                                            }
                                        });
                            }
                        });
    }

    @Override
    public Observable<AuthenticationResponse> signUp(String userType,
                                                     String usersId,
                                                     String password,
                                                     String fcmToken, String firstName,
                                                     String lastName) {
        return authenticationApi.signUp(
                new SignUpRequestBody(
                        userType,
                        usersId,
                        fcmToken,
                        password,
                        true, firstName,
                        lastName
                )).flatMap(new Function<AuthenticationResponse, Observable<AuthenticationResponse>>() {
                        @Override
                        public Observable<AuthenticationResponse> apply(AuthenticationResponse authenticationResponse) {
                            if(authenticationResponse.getUser().isVerified()) {
                                return authenticationApi.getNewTokens(new GetNewTokensBody(authenticationResponse.getUser().getUserId(), password))
                                        .flatMap(new Function<NewTokenResponse, Observable<AuthenticationResponse>>() {
                                            @Override
                                            public Observable<AuthenticationResponse> apply(NewTokenResponse newTokenResponse) {
                                                authenticationResponse.getUser().setRestToken(newTokenResponse.getToken().getRestToken());
                                                authenticationResponse.getUser().setRefreshToken(newTokenResponse.getToken().getRefreshToken());
                                                return Observable.just(authenticationResponse);
                                            }
                                        });
                            }else{
                                return Observable.just(authenticationResponse);
                            }
                        }
                });
    }

    @Override
    public Observable<Boolean> sendVerificationCode(String user_id, String account_type) {
        return authenticationApi.sendVerificationCode(new SendVerificationCodeBody(user_id, account_type))
               .flatMap(basicResponse -> {
                   return Observable.just(true);
               });
    }

    @Override
    public Observable<Account> confirmVerificationCode(String userId, String account_type, String confirmationCode,String password) {
        return authenticationApi.confirmVerificationCode(new VerificationCodeBody(userId,account_type,confirmationCode))
                .flatMap(new Function<ValidationResponse, Observable<Account>>() {
                    @Override
                    public Observable<Account> apply(ValidationResponse validationResponse) {
                        return authenticationApi
                                .getNewTokens(new GetNewTokensBody(validationResponse.getUserResponse().getUserId(), password))
                                .flatMap(new Function<NewTokenResponse, Observable<Account>>() {
                                    @Override
                                    public Observable<Account> apply(NewTokenResponse newTokenResponse) {
                                        validationResponse.getUserResponse().setRestToken(newTokenResponse.getToken().getRestToken());
                                        validationResponse.getUserResponse().setRefreshToken(newTokenResponse.getToken().getRefreshToken());
                                        return Observable.just(accountMapper.mapIn(validationResponse.getUserResponse()));
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<Void> resetPassword(String usersId, String userType) {
        return authenticationApi.resetPassword(new ResetPasswordBody(usersId,deviceISO31662Code,userType)).map(new Function<AuthenticationResponse, Void>() {
            @Override
            public Void apply(AuthenticationResponse authenticationResponse) {
                return null;
            }
        });

    }

    @Override
    public Observable<Boolean> sendCodeForForgotPassword(String phoneNumber) {
        return authenticationApi.sendCodeForForgotPassword(new SendCodeForForgotPasswordBody(phoneNumber,deviceISO31662Code))
                .flatMap(authenticationResponse -> {
                    return Observable.just(true);
                });
    }

    public Observable<Boolean> confirmCodeForForgotPassword(String phoneNumber, String code){
        return authenticationApi.confirmCodeForForgotPassword(new ConfirmCodeForForgotPasswordBody(phoneNumber,code,deviceISO31662Code))
                .flatMap(authenticationResponse -> {
                    return Observable.just(true);
                });
    }


    @Override
    public Observable<User> confirmPassword(String confirmationCode) {
        return null; /*authenticationApi.confirmPassword(new VerificationCodeBody(confirmationCode))
                .map(authenticationResponse -> userMapper.mapIn(authenticationResponse.getUser()));*/
    }

    @Override
    public Observable<Void> logOut() {
        //TODO
        return null;
    }

    @Override
    public Observable<Boolean> deleteAccount() {
        return authenticationApi.deleteUser()
                .map(basicResponse -> basicResponse.getData().length == 0);
    }

    @Override
    public Observable<NewTokenResponse> getNewTokens(String userId, String password) {
        return authenticationApi.getNewTokens(new GetNewTokensBody(userId, password));
    }

}
