package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.body.authentication.AcceptTermAndConditionBody;
import com.lattis.ellipse.data.network.model.body.authentication.GetNewTokensBody;
import com.lattis.ellipse.data.network.model.body.authentication.RefreshTokenBody;
import com.lattis.ellipse.data.network.model.body.authentication.ResetPasswordBody;
import com.lattis.ellipse.data.network.model.body.authentication.SendCodeForForgotPasswordBody;
import com.lattis.ellipse.data.network.model.body.authentication.SendVerificationCodeBody;
import com.lattis.ellipse.data.network.model.body.authentication.SignInRequestBody;
import com.lattis.ellipse.data.network.model.body.authentication.SignUpRequestBody;
import com.lattis.ellipse.data.network.model.body.commun.VerificationCodeBody;
import com.lattis.ellipse.data.network.model.response.AuthenticationResponse;
import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.CheckTermsConditionResponse;
import com.lattis.ellipse.data.network.model.response.ConfirmCodeForForgotPasswordBody;
import com.lattis.ellipse.data.network.model.response.GetTermsAndConditionsResponse;
import com.lattis.ellipse.data.network.model.response.NewTokenResponse;
import com.lattis.ellipse.data.network.model.response.RefreshTokenResponse;
import com.lattis.ellipse.data.network.model.response.ValidationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface AuthenticationApi {

    @POST("users/registration/")
    Observable<AuthenticationResponse> signIn(@Body SignInRequestBody body);

    @POST("users/registration/")
    Observable<AuthenticationResponse> signUp(@Body SignUpRequestBody body);


    @POST("users/email-verification-code/")
    Observable<BasicResponse> sendVerificationCode(@Body SendVerificationCodeBody sendVerificationCodeBody);

    @POST("users/confirm-email-verification-code/")
    Observable<ValidationResponse> confirmVerificationCode(@Body VerificationCodeBody verificationCodeBody);



    @POST("users/forgot-password-code/")
    Observable<AuthenticationResponse> resetPassword(@Body ResetPasswordBody resetPasswordBody);

    @POST("users/forgot-password-code/")
    Observable<AuthenticationResponse> sendCodeForForgotPassword(@Body SendCodeForForgotPasswordBody sendCodeForForgotPasswordBody);

    @POST("users/confirm-forgot-password-code/")
    Observable<AuthenticationResponse> confirmCodeForForgotPassword(@Body ConfirmCodeForForgotPasswordBody confirmCodeForForgotPasswordBody);

    @GET("users/delete-account/")
    Observable<BasicResponse> deleteUser();

    @GET("users/terms-and-conditions/")
    Observable<GetTermsAndConditionsResponse> getTermsAndCondition();

    @POST("users/accept-terms-and-conditions/")
    Observable<BasicResponse> acceptTermsAndCondition(@Body AcceptTermAndConditionBody acceptTermAndConditionBody);

    @GET("users/check-accepted-terms-and-conditions/")
    Observable<CheckTermsConditionResponse> checkTermsAndCondition();

    @POST("users/new-tokens")
    Observable<NewTokenResponse> getNewTokens(@Body GetNewTokensBody getNewTokensBody);

    @POST("users/refresh-tokens")
    Call<RefreshTokenResponse> refreshToken(@Body RefreshTokenBody refreshTokenBody);

}
