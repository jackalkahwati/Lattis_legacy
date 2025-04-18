package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.AuthenticationResponse;
import com.lattis.ellipse.data.network.model.response.NewTokenResponse;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.domain.model.User;

import io.reactivex.Observable;

public interface Authenticator {

    Observable<Account> signIn(String userType,
                                              String usersId,
                                              String password,
                                              String fcmRegistrationId);

    Observable<AuthenticationResponse> signUp(String userType,
                                              String usersId,
                                              String password,
                                              String fcmRegistrationId, String firstName,
                                              String lastName);

    Observable<Boolean> sendVerificationCode(String user_id, String account_type);

    Observable<Account> confirmVerificationCode(String userId, String account_type, String confirmationCode, String password);


    Observable<Void> resetPassword(String usersId, String userType);

    Observable<User> confirmPassword(String confirmationCode);

    Observable<Void> logOut();

    Observable<Boolean> deleteAccount();

    Observable<Boolean> sendCodeForForgotPassword(String phoneNumber);

    Observable<Boolean>  confirmCodeForForgotPassword(String phoneNumber, String code);

    public Observable<NewTokenResponse> getNewTokens(String userId, String password);

}
