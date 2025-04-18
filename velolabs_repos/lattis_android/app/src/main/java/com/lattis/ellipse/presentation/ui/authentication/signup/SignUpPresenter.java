package com.lattis.ellipse.presentation.ui.authentication.signup;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.Utils.FirebaseUtil;
import com.lattis.ellipse.domain.interactor.authentication.SignUpUseCase;
import com.lattis.ellipse.domain.interactor.error.SignUpValidationError;
import com.lattis.ellipse.domain.model.VerificationBundle;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;
import retrofit2.adapter.rxjava2.HttpException;

import static com.lattis.ellipse.domain.interactor.error.SignUpValidationError.Status.INVALID_EMAIL;
import static com.lattis.ellipse.domain.interactor.error.SignUpValidationError.Status.INVALID_PASSWORD;

public class SignUpPresenter extends ActivityPresenter<SignUpView> {

    private String email;
    private String firstName;
    private String lastName;


    private String password;
    private SignUpUseCase signUpUseCase;
    private final String SIGNUP_CONTENT = "SignUpError";
    private static final String TAG = SignUpPresenter.class.getSimpleName();

    @Inject
    SignUpPresenter(SignUpUseCase signUpUseCase) {
        this.signUpUseCase = signUpUseCase;
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
    }

    @Override
    protected void updateViewState() {
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }


    void setLastName(String lastName) {
        this.lastName = lastName;
    }

    void trySignUp() {
        subscriptions.add(signUpUseCase.withValue(email, firstName, lastName, password)
                .execute(new RxObserver<VerificationBundle>(view) {
                    @Override
                    public void onNext(VerificationBundle verificationBundle) {
                        super.onNext(verificationBundle);
                        if (!verificationBundle.isVerified()) {
                            view.showRequestCode(verificationBundle.getUserId());
                        } else {
                            view.showHomePage(verificationBundle.getUserId());
                        }
                        FirebaseUtil.getInstance().addSignUpEvent(verificationBundle.getUserId(), email);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e!=null && e instanceof SignUpValidationError) {
                            if(view!=null)
                                view.onRegistrationFailed();
                        } else if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 401) {

                                if(view!=null)
                                    view.showDuplicateError();
                            } else {
                                if(view!=null)
                                    view.onRegistrationFailed();
                            }
                        } else {
                            if(view!=null)
                                view.onRegistrationFailed();
                        }
                    }
                }));
    }


    private void onValidationError(SignUpValidationError error) {
        List<SignUpValidationError.Status> status = error.getStatus();


        if (status.contains(INVALID_PASSWORD)) {
            if(view!=null)
                view.showPasswordError(R.string.error_invalid_password);
        } else {
            if(view!=null)
                view.hidePasswordError();
        }

        if (status.contains(INVALID_EMAIL)) {
            if(view!=null)
                view.showEmailError(R.string.error_invalid_email);
        } else {
            if(view!=null)
                view.hideEmailError();
        }
    }
}
