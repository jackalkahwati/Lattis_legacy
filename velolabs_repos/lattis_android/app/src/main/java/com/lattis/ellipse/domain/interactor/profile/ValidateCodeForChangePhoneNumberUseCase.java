package com.lattis.ellipse.domain.interactor.profile;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.lattis.ellipse.data.UserDataRepository;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;
import com.lattis.ellipse.presentation.dagger.qualifier.ISO31662Code;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ValidateCodeForChangePhoneNumberUseCase extends UseCase<Boolean> {

    private UserRepository userRepository;

    private String phoneNumberPrefix;
    private String code;
    private String phoneNumber;
    private PhoneNumberUtil phoneNumberUtil;
    private String countryCode;

    public ValidateCodeForChangePhoneNumberUseCase withCode(String code) {
        this.code = code;
        return this;
    }
    public ValidateCodeForChangePhoneNumberUseCase withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    @Inject
    public ValidateCodeForChangePhoneNumberUseCase(ThreadExecutor threadExecutor,
                                                   PostExecutionThread postExecutionThread,
                                                   UserDataRepository userDataRepository,
                                                   PhoneNumberUtil phoneNumberUtil,
                                                   @ISO31662Code String defaultIsoCode) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
        this.phoneNumberUtil = phoneNumberUtil;
        this.countryCode = defaultIsoCode;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return userRepository.validateCodeForChangePhoneNumber(code,getFormattedPhoneNumber(phoneNumber));
    }

    protected String getFormattedPhoneNumber(String phoneNumber) {
        Phonenumber.PhoneNumber parsedPhoneNumber = null;
        try {
            parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode);
            return phoneNumberUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
