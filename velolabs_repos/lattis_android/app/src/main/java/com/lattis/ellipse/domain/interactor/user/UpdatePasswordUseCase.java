package com.lattis.ellipse.domain.interactor.user;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdatePasswordUseCase extends com.lattis.ellipse.domain.interactor.UseCase<Boolean> {

    private UserRepository userRepository;
    private  String password;
    private String new_password;

    @Inject
    protected UpdatePasswordUseCase(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }
    public UpdatePasswordUseCase withPassword(String password) {
        this.password = password;
        return this;
    }

    public UpdatePasswordUseCase withNewPassword(String newPassword) {
        this.new_password = newPassword;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return userRepository.changePassword(password,new_password);
    }
}
