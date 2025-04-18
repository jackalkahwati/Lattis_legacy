package com.lattis.ellipse.domain.interactor.authentication;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.presentation.dagger.qualifier.ISDCode;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 3/17/17.
 */

public class ConfirmCodeForForgotPasswordUseCase extends UseCase<Boolean> {

    private String phoneNumber;
    private Authenticator authenticator;
    private String code;

    @Inject
    public ConfirmCodeForForgotPasswordUseCase(ThreadExecutor threadExecutor,
                                               PostExecutionThread postExecutionThread,
                                               Authenticator authenticator,
                                               PhoneNumberUtil phoneNumberUtil, @ISDCode int phoneNumberPrefix) {
        super(threadExecutor, postExecutionThread);
        this.authenticator = authenticator;
    }

    public ConfirmCodeForForgotPasswordUseCase withValue(String phoneNumber, String code) {
        this.phoneNumber = phoneNumber;
        this.code=code;
        return this;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return  authenticator.confirmCodeForForgotPassword(phoneNumber,code);
    }

}
