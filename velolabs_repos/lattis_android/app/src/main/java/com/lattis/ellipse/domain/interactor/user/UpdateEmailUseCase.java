package com.lattis.ellipse.domain.interactor.user;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdateEmailUseCase extends com.lattis.ellipse.domain.interactor.UseCase<Boolean> {

    private UserRepository userRepository;
    private  String email;

    @Inject
    protected UpdateEmailUseCase(ThreadExecutor threadExecutor,
                                 PostExecutionThread postExecutionThread,
                                 UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }
    public UpdateEmailUseCase withEmail(String email) {
        this.email = email;
        return this;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return userRepository.sendCodeToUpdateEmail(email);
    }
}
