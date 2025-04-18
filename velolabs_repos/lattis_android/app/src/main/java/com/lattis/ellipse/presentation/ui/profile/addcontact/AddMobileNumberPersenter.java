package com.lattis.ellipse.presentation.ui.profile.addcontact;

import com.lattis.ellipse.domain.interactor.error.UpdateNumberValidationError;
import com.lattis.ellipse.domain.interactor.profile.SendCodeToPhoneNumberUseCase;
import com.lattis.ellipse.presentation.dagger.qualifier.ISDCode;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.lattis.ellipse.R;

/**
 * Created by lattis on 29/04/17.
 */

public class AddMobileNumberPersenter  extends ActivityPresenter<AddMobileNumberView> {
    private int phoneNumberCountryPrefix;
    private String phoneNumber;

    private SendCodeToPhoneNumberUseCase sendCodeToPhoneNumberUseCase;

    @Inject
    AddMobileNumberPersenter(@ISDCode int phoneNumberPrefix,
                             SendCodeToPhoneNumberUseCase sendCodeToPhoneNumberUseCase)
    {
        this.phoneNumberCountryPrefix = phoneNumberPrefix;
        this.sendCodeToPhoneNumberUseCase = sendCodeToPhoneNumberUseCase;

    }
    @Override
    protected void updateViewState() {
        super.updateViewState();
        view.showPhoneNumberCountryPrefix(this.phoneNumberCountryPrefix);
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void sendCodeToPhoneNumber() {
        subscriptions.add(sendCodeToPhoneNumberUseCase
                .withPhoneNumber(phoneNumber)
                .execute(new RxObserver<Boolean>(view, false) {
                    @Override
                    public void onNext(Boolean status) {
                        view.onPhoneNumberUpdated();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof UpdateNumberValidationError) {
                            onValidationError((UpdateNumberValidationError) e);
                        } else {
                            view.onPhoneNumberUpdateFail();
                        }
                        //view.onPhoneNumberUpdated();    //Remove this added  {"error":{"name":"APILimitExceeded","message":"Too many request made to API","code":400},"status":400,"payload":null}
                    }
                }));
    }



    private void onValidationError(UpdateNumberValidationError error) {
        List<UpdateNumberValidationError.Status> status = error.getStatus();
        if (status.contains(UpdateNumberValidationError.Status.INVALID_PHONE_NUMBER)) {
            view.showPhoneNumberError(R.string.error_invalid_mobilenumber);
        } else {
            view.hidePhoneNumberError();
        }
    }

}
