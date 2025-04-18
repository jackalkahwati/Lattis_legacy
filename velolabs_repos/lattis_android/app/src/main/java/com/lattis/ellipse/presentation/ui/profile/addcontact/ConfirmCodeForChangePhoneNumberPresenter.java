package com.lattis.ellipse.presentation.ui.profile.addcontact;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;

import com.lattis.ellipse.domain.interactor.profile.SendCodeToPhoneNumberUseCase;
import com.lattis.ellipse.domain.interactor.profile.ValidateCodeForChangePhoneNumberUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;


public class ConfirmCodeForChangePhoneNumberPresenter extends ActivityPresenter<ConfirmCodeForChangePhoneNumberView> {


    private String TAG = ConfirmCodeForChangePhoneNumberPresenter.class.getSimpleName();

    private ValidateCodeForChangePhoneNumberUseCase validateCodeForChangePhoneNumberUseCase;
    private SendCodeToPhoneNumberUseCase sendCodeToPhoneNumberUseCase;


    private String phoneNumber;
    private String code;


    @Inject
    public ConfirmCodeForChangePhoneNumberPresenter(ValidateCodeForChangePhoneNumberUseCase validateCodeForChangePhoneNumberUseCase,
                                                    SendCodeToPhoneNumberUseCase sendCodeToPhoneNumberUseCase) {
        this.validateCodeForChangePhoneNumberUseCase = validateCodeForChangePhoneNumberUseCase;
        this.sendCodeToPhoneNumberUseCase = sendCodeToPhoneNumberUseCase;
    }

    public void setConfirmCode(String confirmCode){
        code = confirmCode;
    }

    public void updateCodePhoneNumber() {

        Log.e(TAG, "Phone number: "+ phoneNumber +" Code: "+code);

        subscriptions.add(validateCodeForChangePhoneNumberUseCase
                .withCode(code)
                .withPhoneNumber(phoneNumber)
                .execute(new RxObserver<Boolean>(view, false) {
                    @Override
                    public void onNext(Boolean status) {
                        view.onCodePhoneNumberUpdated();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                    }
                }));
    }

    public void reSendCode(){
            subscriptions.add(sendCodeToPhoneNumberUseCase
                    .withPhoneNumber(phoneNumber)
                    .execute(new RxObserver<Boolean>(view, false) {
                        @Override
                        public void onNext(Boolean status) {
                            view.onSendSuccessful();
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
        Log.e(TAG,"Bundle are received");
        if(arguments!=null){
            phoneNumber = arguments.getString("PHONE_NUMBER");
        }

        view.updateVerificationInfoText(phoneNumber);

    }

    @Override
    protected void updateViewState() {

    }


}
