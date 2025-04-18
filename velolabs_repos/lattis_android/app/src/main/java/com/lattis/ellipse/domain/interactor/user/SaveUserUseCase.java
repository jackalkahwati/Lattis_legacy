package com.lattis.ellipse.domain.interactor.user;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.repository.UserRepository;

import io.reactivex.Observable;

public class SaveUserUseCase extends UseCase<User> {

    private UserRepository userRepository;
    private User user;

    protected SaveUserUseCase(ThreadExecutor threadExecutor,
                              PostExecutionThread postExecutionThread,
                              UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public SaveUserUseCase withValue(User user) {
        this.user = user;
        return this;
    }

    @Override
    protected Observable<User> buildUseCaseObservable() {
        return userRepository.saveUser(user);
    }
}
