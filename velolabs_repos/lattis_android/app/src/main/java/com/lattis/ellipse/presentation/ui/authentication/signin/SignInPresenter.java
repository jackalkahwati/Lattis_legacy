package com.lattis.ellipse.presentation.ui.authentication.signin;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.Utils.FirebaseUtil;
import com.lattis.ellipse.domain.interactor.authentication.SendCodeForForgotPasswordUseCase;
import com.lattis.ellipse.domain.interactor.authentication.SignInUseCase;
import com.lattis.ellipse.domain.interactor.error.SignInValidationError;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;
import retrofit2.adapter.rxjava2.HttpException;

public class SignInPresenter extends ActivityPresenter<SignInView> {

    private String email;


    private String password;
    private SignInUseCase signInUseCase;
    private SendCodeForForgotPasswordUseCase sendCodeForForgotPasswordUseCase;
    private final String SIGNIN_CONTENT = "SignInError";
    private static final String TAG = SignInPresenter.class.getSimpleName();


    @Inject
    SignInPresenter(SignInUseCase signInUseCase, SendCodeForForgotPasswordUseCase sendCodeForForgotPasswordUseCase) {
        this.signInUseCase = signInUseCase;
        this.sendCodeForForgotPasswordUseCase = sendCodeForForgotPasswordUseCase;
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

    void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }


    void trySignIn() {
        subscriptions.add(signInUseCase.withValues(email, password)
                .execute(new RxObserver<User>(view, false) {
                    @Override
                    public void onNext(User user) {
                        super.onNext(user);
                        FirebaseUtil.getInstance().addSignInEvent(user.getId(), email);
                        if (user.isVerified()) {
                            if(view!=null) {
                                view.onUserVerified(user.getId());
                            }
                        } else {
                            if(view!=null) {
                                view.onUserNotVerified(user.getId());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (view!=null && e!=null && e instanceof SignInValidationError) {
                            onValidationError((SignInValidationError) e);
                        } else if (view!=null && e!=null && e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 404) {
                                if(view!=null)
                                    view.onUserNotExists();
                            } else {
                                if(view!=null)
                                    view.onAuthenticationFailed();
                            }
                        } else {
                            if(view!=null) {
                                view.onAuthenticationFailed();
                            }
                        }
                    }
                }));
    }

    private void onValidationError(SignInValidationError error) {
        List<SignInValidationError.Status> status = error.getStatus();

        if (status.contains(SignInValidationError.Status.INVALID_PASSWORD)) {
            if(view!=null) {
                view.showPasswordError(R.string.error_invalid_password);
            }
        } else {
            if(view!=null) {
                view.hidePasswordError();
            }
        }
    }

}
