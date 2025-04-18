package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class DeleteAccountUseCase extends UseCase<Boolean> {

    private UserRepository userRepository;

    @Inject
    DeleteAccountUseCase(ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread,
                         UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.userRepository.deleteUserAccount();
    }

}
