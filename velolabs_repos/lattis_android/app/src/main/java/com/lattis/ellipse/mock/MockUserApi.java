package com.lattis.ellipse.mock;

import android.content.Context;

import com.lattis.ellipse.data.network.api.UserApi;
import com.lattis.ellipse.data.network.model.body.authentication.AcceptTermAndConditionBody;
import com.lattis.ellipse.data.network.model.body.commun.VerificationCodeBody;
import com.lattis.ellipse.data.network.model.body.user.ChangePasswordBody;
import com.lattis.ellipse.data.network.model.body.user.GetUserCurrentStatusBody;
import com.lattis.ellipse.data.network.model.body.user.SendCodeUpdatePhoneNumberBody;
import com.lattis.ellipse.data.network.model.body.user.SendForgotPasswordCodeBody;
import com.lattis.ellipse.data.network.model.body.user.UpdateEmailCodeBody;
import com.lattis.ellipse.data.network.model.body.user.UpdateUserBody;
import com.lattis.ellipse.data.network.model.body.user.ValidateCodeForChangePhoneNumberBody;
import com.lattis.ellipse.data.network.model.body.user.ValidateCodeUpdateEmailBody;
import com.lattis.ellipse.data.network.model.response.AddPrivateNetworkResponse;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.CheckTermsConditionResponse;
import com.lattis.ellipse.data.network.model.response.ConfirmCodeForForgotPasswordBody;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.data.network.model.response.GetTermsAndConditionsResponse;
import com.lattis.ellipse.data.network.model.response.GetUserResponse;

import retrofit2.http.Body;
import retrofit2.mock.BehaviorDelegate;
import io.reactivex.Observable;

public class MockUserApi implements UserApi {

    private Context context;
    private final BehaviorDelegate<UserApi> delegate;

    public MockUserApi(Context context, BehaviorDelegate<UserApi> delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    public Observable<GetUserResponse> getUser() {
        GetUserResponse response = MockUtils.jsonAssetToObject(context, "get_user_200_success.json", GetUserResponse.class);
        return delegate.returningResponse(response.getUserResponse()).getUser();
    }

    @Override
    public Observable<GetUserResponse> saveUser(@Body UpdateUserBody updateUserBody) {
        return null;
    }


    @Override
    public Observable<Void> validateCodeForChangePhoneNumber(@Body ValidateCodeForChangePhoneNumberBody body) {
        return null;
    }

    @Override
    public Observable<Void> sendCodeForPhoneNumber(@Body SendCodeUpdatePhoneNumberBody body) {
        return null;
    }

    @Override
    public Observable<Void> sendCodeToUpdatePassword() {
        return null;
    }

    @Override
    public Observable<Void> validateCodeForChangePassword(@Body ChangePasswordBody body) {
        return null;
    }

    @Override
    public Observable<GetCurrentUserStatusResponse> getCurrentUserStatus(@Body GetUserCurrentStatusBody getUserCurrentStatusBody) {
        return null;
    }

    @Override
    public Observable<Void> sendCodeForUpdateEmail(@Body UpdateEmailCodeBody updateEmailCodeBody) {
        return null;
    }

    @Override
    public Observable<AddPrivateNetworkResponse> addPrivateNetworkEmail(@Body UpdateEmailCodeBody updateEmailCodeBody) {
        return null;
    }

    @Override
    public Observable<BasicResponse> deleteUserAccount() {
        return null;
    }

    @Override
    public Observable<GetTermsAndConditionsResponse> getTermsAndCondition() {
        return null;
    }

    @Override
    public Observable<BasicResponse> acceptTermsAndCondition(@Body AcceptTermAndConditionBody acceptTermAndConditionBody) {
        return null;
    }

    @Override
    public Observable<CheckTermsConditionResponse> checkTermsAndCondition() {
        return null;
    }

    @Override
    public Observable<BasicResponse> sendForgotPasswordCode(@Body SendForgotPasswordCodeBody sendForgotPasswordCodeBody) {
        return null;
    }

    @Override
    public Observable<BasicResponse> confirmCodeForForgotPassword(@Body ConfirmCodeForForgotPasswordBody confirmCodeForForgotPasswordBody) {
        return null;
    }
    @Override
    public Observable<Void> validateCodeForChangeMail(@Body ValidateCodeUpdateEmailBody validateCodeUpdateEmailBody) {
        return null;
    }

    @Override
    public Observable<Void> confirmVerificationCodeForPrivateNetwork(@Body VerificationCodeBody verificationCodeBody) {
        return null;
    }
}
