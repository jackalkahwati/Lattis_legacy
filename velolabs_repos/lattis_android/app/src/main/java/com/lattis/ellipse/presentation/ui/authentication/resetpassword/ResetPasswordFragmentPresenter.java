package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.interactor.authentication.ResetPasswordUseCase;
import com.lattis.ellipse.domain.interactor.error.ResetPasswordValidationError;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;
import retrofit2.adapter.rxjava2.HttpException;

import static com.lattis.ellipse.presentation.ui.authentication.resetpassword.ConfirmCodeForForgotPasswordFragment.ARG_EMAIL;
import static com.lattis.ellipse.presentation.ui.authentication.resetpassword.ResetPasswordFragment.ARG_CONFIRMATION_CODE;

public class ResetPasswordFragmentPresenter extends FragmentPresenter<ResetPasswordFragmentView> {

    private ResetPasswordUseCase resetPasswordUseCase;


    private String email;
    private String confirmationCode;
    private String password;
    private String confirmPassword;
    private final String ResetPasswordCodeVerification = "ResetPasswordError";
    private static final String TAG = ResetPasswordFragmentPresenter.class.getSimpleName();

    @Inject
    public ResetPasswordFragmentPresenter(ResetPasswordUseCase resetPasswordUseCase) {
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @Override
    protected void updateViewState() {

    }


    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            this.email = arguments.getString(ARG_EMAIL);
            this.confirmationCode = arguments.getString(ARG_CONFIRMATION_CODE);

        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void resetPassword() {

        if (!password.equals(confirmPassword)) {
            view.showPasswordError(R.string.error_incorrect_password);
            return;
        }


        subscriptions.add(resetPasswordUseCase
                .withValues(email, confirmationCode, password)
                .execute(new RxObserver<BasicResponse>(view, false) {
                    @Override
                    public void onNext(BasicResponse basicResponse) {
                        super.onNext(basicResponse);
                        view.onPasswordChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof ResetPasswordValidationError) {
                            onValidationError((ResetPasswordValidationError) e);
                        } else {
                            if(view!=null)
                                view.showFailToSaveDialog();
                        }
                    }
                }));
    }

    private void onValidationError(ResetPasswordValidationError error) {
        List<ResetPasswordValidationError.Status> status = error.getStatus();
        if (status.contains(ResetPasswordValidationError.Status.INVALID_PASSWORD)) {
            view.showPasswordError(R.string.error_incorrect_password);
        } else {
            view.hidePasswordError();
        }
    }


}
