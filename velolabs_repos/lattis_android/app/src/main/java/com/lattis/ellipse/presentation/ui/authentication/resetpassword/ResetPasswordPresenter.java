package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.interactor.user.SendForgotPasswordCodeUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;


public class ResetPasswordPresenter extends ActivityPresenter<ResetPasswordView> {


    private String email;
    private final SendForgotPasswordCodeUseCase sendForgotPasswordCodeUseCase;
    private final String ResetPassword = "sendVerficationCodeError";
    private static final String TAG = ResetPasswordPresenter.class.getSimpleName();


    @Inject
    public ResetPasswordPresenter(SendForgotPasswordCodeUseCase sendForgotPasswordCodeUseCase) {
        this.sendForgotPasswordCodeUseCase = sendForgotPasswordCodeUseCase;
    }

    @Override
    protected void updateViewState() {

    }

    void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


    void sendCodeForForgotPassword() {
        subscriptions.add(sendForgotPasswordCodeUseCase
                .toEmail(email)
                .execute(new RxObserver<BasicResponse>(view, false) {
                    @Override
                    public void onNext(BasicResponse basicResponse) {
                        super.onNext(basicResponse);
                        view.onSendForgotPasswordCodeSuccess(email);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onSendForgotPasswordCodeFailure();
                    }
                }));
    }

}
