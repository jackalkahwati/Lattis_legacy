package com.lattis.ellipse.presentation.ui.authentication.verification.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.domain.interactor.authentication.ConfirmVerificationUseCase;
import com.lattis.ellipse.domain.interactor.authentication.GetVerificationCodeUseCase;
import com.lattis.ellipse.domain.interactor.error.ConfirmCodeValidationError;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;

import static com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity.ARG_PASSWORD;
import static com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity.ARG_USER_ACCOUNT_TYPE;
import static com.lattis.ellipse.presentation.ui.authentication.verification.fragment.EnterSecretCodeActivity.ARG_USER_ID;

public class EnterSecretCodePresenter extends ActivityPresenter<EnterSecretCodeView> {

    private String confirmationCode;
    private String account_type;
    private String userId;
    private String password;

    private GetVerificationCodeUseCase getVerificationCodeUseCase;
    private ConfirmVerificationUseCase confirmVerificationUseCase;

    @Inject
    public EnterSecretCodePresenter(GetVerificationCodeUseCase getVerificationCodeUseCase,
                                    ConfirmVerificationUseCase confirmVerificationUseCase) {
        this.getVerificationCodeUseCase = getVerificationCodeUseCase;
        this.confirmVerificationUseCase = confirmVerificationUseCase;
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null && arguments.containsKey(ARG_USER_ID)) {
            this.userId = arguments.getString(ARG_USER_ID);
        }
        if (arguments != null && arguments.containsKey(ARG_USER_ACCOUNT_TYPE)) {
            this.account_type = arguments.getString(ARG_USER_ACCOUNT_TYPE);
        }

        if (arguments != null && arguments.containsKey(ARG_PASSWORD)) {
            this.password = arguments.getString(ARG_PASSWORD);
        }


    }

    @Override
    protected void updateViewState() {
        sendConfirmationCode();
    }

    void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    void submitConfirmationCode() {
        subscriptions.add(confirmVerificationUseCase
                .forUser(userId)
                .forAccountType(account_type)
                .withConfirmationCode(confirmationCode)
                .withPassword(password)
                .execute(new RxObserver<User>(view) {
                    @Override
                    public void onNext(User user) {
                        view.onSecretCodeConfirmed();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onSecretCodeFail();
                        if (e!=null && e instanceof ConfirmCodeValidationError) {
                            onValidationError((ConfirmCodeValidationError) e);
                        }
                    }
                }));
    }

    void sendConfirmationCode() {
        subscriptions.add(getVerificationCodeUseCase.forUser(userId)
                .forAccountType(account_type)
                .execute(new RxObserver<Boolean>(view, false)));
    }

    private void onValidationError(ConfirmCodeValidationError error) {
        List<ConfirmCodeValidationError.Status> status = error.getStatus();
        if (status.contains(ConfirmCodeValidationError.Status.INVALID_CONFIRMATION_CODE)) {
            view.showConfirmationCodeError(R.string.error_confirmation_code);
        } else {
            view.hideConfirmationCodeError();
        }
    }
}
