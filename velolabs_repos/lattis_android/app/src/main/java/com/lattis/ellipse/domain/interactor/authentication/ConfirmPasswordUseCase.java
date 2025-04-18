package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import io.reactivex.Observable;

public class ConfirmPasswordUseCase extends UseCase<Void> {

    private UserRepository userRepository;
    private String phoneNumber;

    public ConfirmPasswordUseCase(ThreadExecutor threadExecutor,
                                  PostExecutionThread postExecutionThread,
                                  UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public ConfirmPasswordUseCase withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return null;//userRepository.resendConfirmationCode(phoneNumber);
    }

}
