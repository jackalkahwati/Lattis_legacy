package com.lattis.ellipse.domain.interactor.profile;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.lattis.ellipse.data.UserDataRepository;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.UpdateNumberValidationError;
import com.lattis.ellipse.domain.repository.UserRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.ISO31662Code;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.lattis.ellipse.domain.interactor.error.UpdateNumberValidationError.Status.INVALID_PHONE_NUMBER;

public class SendCodeToPhoneNumberUseCase extends UseCase<Boolean> {

    private UserRepository userRepository;
    private PhoneNumberUtil phoneNumberUtil;

    private String countryCode;
    private String phoneNumber;

    @Inject
    public SendCodeToPhoneNumberUseCase(ThreadExecutor threadExecutor,
                                        PostExecutionThread postExecutionThread,
                                        UserDataRepository userDataRepository,
                                        PhoneNumberUtil phoneNumberUtil,
                                        @ISO31662Code String defaultIsoCode) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
        this.phoneNumberUtil = phoneNumberUtil;
        this.countryCode = defaultIsoCode;
    }

    public SendCodeToPhoneNumberUseCase withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        List<UpdateNumberValidationError.Status> errors = getErrors();
        if (errors.isEmpty()) {
            return userRepository.sendCodeToUpdatePhoneNumber(countryCode, phoneNumber);
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

    protected boolean isPhoneNumberInvalid() {
        Phonenumber.PhoneNumber parsedPhoneNumber = null;
        try {
            parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode);
            phoneNumber = phoneNumberUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            return false;
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return true;
    }

}
