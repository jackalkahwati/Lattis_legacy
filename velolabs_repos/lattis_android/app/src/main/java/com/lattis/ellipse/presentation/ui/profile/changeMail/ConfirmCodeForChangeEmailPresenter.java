package com.lattis.ellipse.presentation.ui.profile.changeMail;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.domain.interactor.authentication.ConfirmVerificationForPrivateNetworkUseCase;
import com.lattis.ellipse.domain.interactor.error.ConfirmCodeValidationError;
import com.lattis.ellipse.domain.interactor.profile.ValidateCodeForChangeMailUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;


public class ConfirmCodeForChangeEmailPresenter extends ActivityPresenter<ConfirmCodeForChangeEmailView> {


    private String TAG = ConfirmCodeForChangeEmailPresenter.class.getSimpleName();
    private ConfirmVerificationForPrivateNetworkUseCase confirmVerificationForPrivateNetworkUseCase;
    private ValidateCodeForChangeMailUseCase validateCodeForChangeMailUseCase;
    private String ARG_USER_ACCOUNT_TYPE = "ARG_USER_ACCOUNT_TYPE";
    private String ACCOUNT_TYPE = null;
    private  boolean isPrivate = false;
    private String USER_ACCOUNT_TYPE_PRIVATE = "private_account";

    private String email;
    private String confirmationCode;
    private String userId;


    @Inject
    public ConfirmCodeForChangeEmailPresenter(ValidateCodeForChangeMailUseCase validateCodeForChangeMailUseCase,
                                              ConfirmVerificationForPrivateNetworkUseCase confirmVerificationForPrivateNetworkUseCase
                                              ) {
        this.validateCodeForChangeMailUseCase = validateCodeForChangeMailUseCase;
        this.confirmVerificationForPrivateNetworkUseCase = confirmVerificationForPrivateNetworkUseCase;
    }

    public void setConfirmCode(String confirmCode){
        this.confirmationCode = confirmCode;
    }


    private void updatePrivateEmail() {
        subscriptions.add(confirmVerificationForPrivateNetworkUseCase
                .forUser(userId)
                .forAccountType(ACCOUNT_TYPE)
                .withConfirmationCode(confirmationCode)
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        view.onCodeEmailUpdated();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof ConfirmCodeValidationError) {
                            onValidationError((ConfirmCodeValidationError) e);
                        }else{
                            view.onCodeEmailUpdateFail();
                        }
                    }
                }));
    }

    private void updateMainAccountEmail() {
        subscriptions.add(validateCodeForChangeMailUseCase
                .withCode(confirmationCode)
                .withEmail(email)
                .execute(new RxObserver<Boolean>(view, false) {
                    @Override
                    public void onNext(Boolean status) {

                        view.onCodeEmailUpdated();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                    }
                }));

    }


    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if(arguments!=null){
            email = arguments.getString("EMAIL");
            ACCOUNT_TYPE = arguments.getString(ARG_USER_ACCOUNT_TYPE);
            userId = arguments.getString("USERID");
            if (ACCOUNT_TYPE.equals(USER_ACCOUNT_TYPE_PRIVATE))
            {
                isPrivate = true;
                view.isPrivateAccount(isPrivate);
            }
            else{
                isPrivate = false;
                view.isPrivateAccount(isPrivate);
            }
        }


    }

    @Override
    protected void updateViewState() {

    }


    public void updateCodeEmail() {
        if (isPrivate)
        {
            updatePrivateEmail();
        }
        else{
            updateMainAccountEmail();
        }
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
