package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.UserApi;
import com.lattis.ellipse.data.network.model.body.authentication.AcceptTermAndConditionBody;
import com.lattis.ellipse.data.network.model.body.commun.VerificationCodeBody;
import com.lattis.ellipse.data.network.model.body.user.ChangePasswordBody;
import com.lattis.ellipse.data.network.model.body.user.GetUserCurrentStatusBody;
import com.lattis.ellipse.data.network.model.body.user.SendCodeUpdatePhoneNumberBody;
import com.lattis.ellipse.data.network.model.body.user.SendForgotPasswordCodeBody;
import com.lattis.ellipse.data.network.model.body.user.UpdateEmailCodeBody;
import com.lattis.ellipse.data.network.model.body.user.UpdateUserBody;
import com.lattis.ellipse.data.network.model.body.user.UserBody;
import com.lattis.ellipse.data.network.model.body.user.ValidateCodeForChangePhoneNumberBody;
import com.lattis.ellipse.data.network.model.body.user.ValidateCodeUpdateEmailBody;
import com.lattis.ellipse.data.network.model.mapper.TermsAndConditionsMapper;
import com.lattis.ellipse.data.network.model.mapper.UserMapper;
import com.lattis.ellipse.data.network.model.response.AddPrivateNetworkResponse;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.CheckTermsConditionResponse;
import com.lattis.ellipse.data.network.model.response.ConfirmCodeForForgotPasswordBody;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.dagger.qualifier.DeviceModel;
import com.lattis.ellipse.presentation.dagger.qualifier.DeviceOS;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UserNetworkDataStore {

    private UserApi userApi;
    private UserMapper userMapper;
    private TermsAndConditionsMapper termsAndConditionsMapper;
    private String deviceModel;
    private String deviceOS;

    @Inject
    public UserNetworkDataStore(UserApi userApi,
                                UserMapper userMapper,
                                TermsAndConditionsMapper termsAndConditionsMapper,
                                @DeviceModel String deviceModel,
                                @DeviceOS String deviceOS) {
        this.userApi = userApi;
        this.userMapper = userMapper;
        this.termsAndConditionsMapper=termsAndConditionsMapper;
        this.deviceModel = deviceModel;
        this.deviceOS = deviceOS;
    }

    public Observable<User> getUser(){
        return userApi.getUser()
                .map(getUserResponse -> userMapper.mapIn(getUserResponse));

    }

    public Observable<User> saveUser(User user){
        return userApi.saveUser(new UpdateUserBody(
                new UserBody(user.getId(),user.getFirstName(),user.getLastName(),user.getEmail())))
                .map(getUserResponse -> userMapper.mapIn(getUserResponse));
    }

    public Observable<Boolean> sendCodeToUpdatePhoneNumber(String countryCode, String phoneNumber){
        return userApi.sendCodeForPhoneNumber(new SendCodeUpdatePhoneNumberBody(countryCode, phoneNumber))
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }

    public Observable<Boolean> validateCodeForChangePhoneNumber(String code, String phoneNumber){
        return userApi.validateCodeForChangePhoneNumber(new ValidateCodeForChangePhoneNumberBody(code,phoneNumber))
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }

    public Observable<Boolean> sendCodeForUpdateEmail(String email){
        return userApi.sendCodeForUpdateEmail(new UpdateEmailCodeBody(email))
                .flatMap(aVoid -> {
            return Observable.just(true);
        });
    }
    public Observable<Boolean> validateCodeForChangeEmail(String code, String email){
        return userApi.validateCodeForChangeMail(new ValidateCodeUpdateEmailBody(code,email))
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }

    public Observable<Boolean> confirmVerificationCodeForPrivateNetwork(String userId, String account_type, String confirmationCode) {
        return userApi.confirmVerificationCodeForPrivateNetwork(new VerificationCodeBody(userId,account_type,confirmationCode))
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }

    public Observable<AddPrivateNetworkResponse> addPrivateNetworkEmail(String email){
        return userApi.addPrivateNetworkEmail(new UpdateEmailCodeBody(email));
    }


    public Observable<Boolean> changePassword(String password, String newPassword){
        return userApi.validateCodeForChangePassword(new ChangePasswordBody(password,newPassword))
                .flatMap(aVoid -> {
                    return Observable.just(true);
                });
    }

    public Observable<GetCurrentUserStatusResponse> getCurrentUserStatus(){
        return userApi.getCurrentUserStatus(new GetUserCurrentStatusBody(deviceModel,deviceOS));
    }


    public Observable<TermsAndConditions> getTermsAndConditions() {
        return userApi.getTermsAndCondition()
                .map(getTermsAndConditionsResponse -> termsAndConditionsMapper.mapIn(getTermsAndConditionsResponse));
    }


    public Observable<Boolean> acceptTermsAndCondition(boolean accepted) {
        return userApi.acceptTermsAndCondition(new AcceptTermAndConditionBody(accepted))
                .map(basicResponse -> basicResponse.getStatus() == 200);
    }


    public Observable<Boolean> checkTermsAndConditionAccepted() {
        return userApi.checkTermsAndCondition()
                .map(CheckTermsConditionResponse::hasAccepted);
    }

    public Observable<BasicResponse> sendForgotPasswordCode(String email) {
        return userApi.sendForgotPasswordCode(new SendForgotPasswordCodeBody(email));
    }

    public Observable<BasicResponse> confirmCodeForForgotPassword(String email, String code, String password){
        return userApi.confirmCodeForForgotPassword(new ConfirmCodeForForgotPasswordBody(email,code, password));
    }


    public Observable<Boolean> deleteUserAccount() {
        return userApi.deleteUserAccount()
                .map(basicResponse -> basicResponse.getStatus() == 200);
    }

}
