package com.lattis.ellipse.domain.interactor.authentication;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.UpdateNumberValidationError;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.domain.utils.StringUtils;
import com.lattis.ellipse.presentation.dagger.qualifier.ISDCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.lattis.ellipse.domain.interactor.error.UpdateNumberValidationError.Status.INVALID_PHONE_NUMBER;

/**
 * Created by ssd3 on 3/17/17.
 */

public class SendCodeForForgotPasswordUseCase  extends UseCase<Boolean> {

    private String phoneNumber;
    private String phoneNumberPrefix;
    private Authenticator authenticator;
    private PhoneNumberUtil phoneNumberUtil;


    @Inject
    public SendCodeForForgotPasswordUseCase(ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread,
                         Authenticator authenticator,
                         PhoneNumberUtil phoneNumberUtil,@ISDCode int phoneNumberPrefix) {
        super(threadExecutor, postExecutionThread);

        this.phoneNumberPrefix = Integer.toString(phoneNumberPrefix);
        this.authenticator = authenticator;
        this.phoneNumberUtil=phoneNumberUtil;
    }

    public SendCodeForForgotPasswordUseCase withValue(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {

        List<UpdateNumberValidationError.Status> errors = getErrors();
        if (errors.isEmpty()) {
            return authenticator.sendCodeForForgotPassword(phoneNumber);
        } else {
            return Observable.create(subscriber -> {
                subscriber.onError(new UpdateNumberValidationError(errors));
            });
        }

    }


    private List<UpdateNumberValidationError.Status> getErrors() {
        List<UpdateNumberValidationError.Status> statuses = new ArrayList<>();
        if (isPhoneNumberInvalid()) {
            statuses.add(INVALID_PHONE_NUMBER);
        }
        return statuses;
    }

    private boolean isPhoneNumberInvalid(){
        Phonenumber.PhoneNumber parsedPhoneNumber = null;
        if(phoneNumberPrefix != null && phoneNumberPrefix.length() > 0) {
            String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(phoneNumberPrefix));
            try {
                if (!Objects.equals(isoCode, "ZZ"))
                    parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, isoCode);
                else
                    return false;
            } catch (NumberParseException e) {
                System.err.println(e);
            }

            return ! StringUtils.isValidPhoneNumber(phoneNumber) ||
                    ! phoneNumberUtil.isValidNumber(parsedPhoneNumber);
        } else {
            return true ;
        }
    }
}
