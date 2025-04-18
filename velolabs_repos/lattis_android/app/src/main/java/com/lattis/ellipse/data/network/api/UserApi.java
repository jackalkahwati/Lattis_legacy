package com.lattis.ellipse.data.network.api;

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
import retrofit2.http.GET;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface UserApi {

    @POST("users/get-user/")
    Observable<GetUserResponse> getUser();

    @POST("users/update-user/")
    Observable<GetUserResponse> saveUser(@Body UpdateUserBody updateUserBody);


    @POST("users/update-phone-number/")
    Observable<Void> validateCodeForChangePhoneNumber(@Body ValidateCodeForChangePhoneNumberBody body);

    @POST("users/update-phone-number-code/")
    Observable<Void> sendCodeForPhoneNumber(@Body SendCodeUpdatePhoneNumberBody body);

    @GET("users/update-password-code/")
    Observable<Void> sendCodeToUpdatePassword();


    @POST("users/change-password/")
    Observable<Void> validateCodeForChangePassword(@Body ChangePasswordBody body);

    @POST("users/get-current-status/")
    Observable<GetCurrentUserStatusResponse> getCurrentUserStatus(@Body GetUserCurrentStatusBody getUserCurrentStatusBody);

    @POST("users/update-email-code/")
    Observable<Void> sendCodeForUpdateEmail(@Body UpdateEmailCodeBody updateEmailCodeBody);

    @POST("users/add-private-account/")
    Observable<AddPrivateNetworkResponse> addPrivateNetworkEmail(@Body UpdateEmailCodeBody updateEmailCodeBody);


    @GET("users/delete-account/")
    Observable<BasicResponse> deleteUserAccount();

    @GET("users/terms-and-conditions/")
    Observable<GetTermsAndConditionsResponse> getTermsAndCondition();

    @POST("users/accept-terms-and-conditions/")
    Observable<BasicResponse> acceptTermsAndCondition(@Body AcceptTermAndConditionBody acceptTermAndConditionBody);

    @GET("users/check-accepted-terms-and-conditions/")
    Observable<CheckTermsConditionResponse> checkTermsAndCondition();

    @POST("users/forgot-password")
    Observable<BasicResponse> sendForgotPasswordCode(@Body SendForgotPasswordCodeBody sendForgotPasswordCodeBody);

    @POST("users/confirm-forgot-password/")
    Observable<BasicResponse> confirmCodeForForgotPassword(@Body ConfirmCodeForForgotPasswordBody confirmCodeForForgotPasswordBody);

    @POST("users/update-email/")
    Observable<Void> validateCodeForChangeMail(@Body ValidateCodeUpdateEmailBody validateCodeUpdateEmailBody);

    @POST("users/confirm-email-verification-code/")
    Observable<Void> confirmVerificationCodeForPrivateNetwork(@Body VerificationCodeBody verificationCodeBody);
}
