package cc.skylock.skylock.operation;

import cc.skylock.skylock.Bean.CheckTermsCondition;
import cc.skylock.skylock.Bean.ForgotPasswordParameter;
import cc.skylock.skylock.Bean.PasswordHintParameter;
import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.TermsAndConditionResponse;
import cc.skylock.skylock.Bean.UserRegistrationParameter;
import cc.skylock.skylock.Bean.UpdateUserDetails;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.Bean.UserVerificationParameter;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by prabhu on 2/2/16.
 */
public interface UserApiService {

    @POST("users/registration/")
    public Call<UserRegistrationResponse> UserCreation(@Body UserRegistrationParameter pojoForUserRegistration);

    @GET("users/sign-in-code/")
    public Call<SuccessResponse> RequestForSendVerificationCode();

    @POST("users/confirm-user-code/")
    public Call<UserRegistrationResponse> RequestForVerifyUser(@Body UserVerificationParameter userVerificationParameter);

    @POST("users/forgot-password-code/")
    public Call<UserRegistrationResponse> SendSecretCodeForPassword(@Body ForgotPasswordParameter forgotPasswordParameter);

    @POST("users/confirm-forgot-password-code/")
    public Call<UserRegistrationResponse> ConfirmPassword(@Body PasswordHintParameter mPasswordHintParameter);

    @POST("users/get-user/")
    public Call<UserRegistrationResponse> GetUserDetails(@Body ForgotPasswordParameter forgotPasswordParameter);

    @POST("users/update-user/")
    public Call<UserRegistrationResponse> UpdateUserDetails(@Body UpdateUserDetails pojoForUserRegistration);


    @GET("users/delete-account/")
    public Call<SuccessResponse> DeleteUserAccount();

    @GET("users/terms-and-conditions/")
    public Call<TermsAndConditionResponse> TermsAndCondition();

    @GET("users/accept-terms-and-conditions/")
    public Call<SuccessResponse> AcceptTermsAndCondition();

    @GET("users/check-accepted-terms-and-conditions/")
    public Call<CheckTermsCondition> CheckTermsAndCondition();


}

