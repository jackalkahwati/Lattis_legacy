package com.lattis.ellipse.domain.interactor.profile;

import com.lattis.ellipse.data.UserDataRepository;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ValidateCodeForPrivateMailUseCase extends UseCase<Boolean> {

    private UserRepository userRepository;

    private String code;
    private String email;

    public ValidateCodeForPrivateMailUseCase withCode(String code) {
        this.code = code;
        return this;
    }
    public ValidateCodeForPrivateMailUseCase withEmail(String email) {
        this.email = email;
        return this;
    }

    @Inject
    public ValidateCodeForPrivateMailUseCase(ThreadExecutor threadExecutor,
                                             PostExecutionThread postExecutionThread,
                                             UserDataRepository userDataRepository
                                            ) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return userRepository.validateCodeForChangeEmail(code,email);
    }

}
