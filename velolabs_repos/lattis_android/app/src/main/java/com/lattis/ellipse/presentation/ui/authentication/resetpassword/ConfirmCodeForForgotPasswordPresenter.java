package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.interactor.user.SendForgotPasswordCodeUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;

import javax.inject.Inject;

import static com.lattis.ellipse.presentation.ui.authentication.resetpassword.ConfirmCodeForForgotPasswordFragment.ARG_EMAIL;


public class ConfirmCodeForForgotPasswordPresenter extends FragmentPresenter<ConfirmCodeForForgotPasswordView> {

    private String email;
    private String code;

    private SendForgotPasswordCodeUseCase sendForgotPasswordCodeUseCase;
    private final String ConfirmCodeForForgotPassword = "ConfirmCodeError";
    private static final String TAG = ConfirmCodeForForgotPasswordPresenter.class.getSimpleName();

    private boolean hasBeenAlreadySent = false;

    @Inject
    public ConfirmCodeForForgotPasswordPresenter(SendForgotPasswordCodeUseCase sendForgotPasswordCodeUseCase) {
        this.sendForgotPasswordCodeUseCase = sendForgotPasswordCodeUseCase;
    }

    @Override
    protected void updateViewState() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        if (arguments != null) {
            this.email = arguments.getString(ARG_EMAIL);
        }
    }

    public void sendConfirmationCode() {
        subscriptions.add(sendForgotPasswordCodeUseCase
                .toEmail(email)
                .execute(new RxObserver<BasicResponse>(view) {
                    @Override
                    public void onNext(BasicResponse basicResponse) {
                        if (hasBeenAlreadySent) {
                            view.onSecretCodeResent();
                        } else {
                            view.onSecretCodeSent();
                        }
                        hasBeenAlreadySent = true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        if(view!=null)
                            view.onSecretCodeResentFailed();

                    }
                }));
    }

}
