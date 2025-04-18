package com.lattis.ellipse.domain.interactor.user;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 5/2/17.
 */

public class SendForgotPasswordCodeUseCase extends UseCase<BasicResponse> {

    private UserRepository userRepository;
    private String email;

    @Inject
    protected SendForgotPasswordCodeUseCase(ThreadExecutor threadExecutor,
                                          PostExecutionThread postExecutionThread,
                                          UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public SendForgotPasswordCodeUseCase toEmail(String email){
        this.email=email;
        return this;
    }

    @Override
    protected Observable<BasicResponse> buildUseCaseObservable() {
        return userRepository.sendForgotPasswordCode(email);
    }

}
