package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.AddPrivateNetworkResponse;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.domain.model.User;

import io.reactivex.Observable;


public interface UserRepository {

    Observable<User> getUser();

    Observable<User> saveUser(User user);

    Observable<User> saveUserLocally(User user);

    Observable<Boolean> validateCodeForChangePhoneNumber(String code, String phoneNumber);

    Observable<Boolean> changePassword(String password, String new_password);

    Observable<Boolean> sendCodeToUpdatePhoneNumber(String countryCode, String phoneNumber);

    Observable<Boolean> sendCodeToUpdateEmail(String email);

    Observable<GetCurrentUserStatusResponse> getCurrentUserStatus();

    Observable<TermsAndConditions> getTermsAndConditions();

    Observable<Boolean> acceptTermsAndCondition(boolean accepted);

    Observable<Boolean> checkTermsAndConditionAccepted();

    Observable<BasicResponse> sendForgotPasswordCode(String email);

    Observable<BasicResponse> confirmCodeForForgotPassword(String email, String code, String password);

    Observable<Boolean> validateCodeForChangeEmail(String code, String email);
    Observable<Boolean> confirmVerificationCodeForPrivateNetwork(String userId, String account_type, String confirmationCode);


    Observable<AddPrivateNetworkResponse> addPrivateNetworkEmail(String email);

    Observable<Boolean> deleteUserAccount();

    Observable<User> getLocalUser();
}
