package com.lattis.ellipse.domain.interactor.user;

import com.lattis.ellipse.data.network.model.response.AddPrivateNetworkResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class AddPrivateNetworkUseCase extends com.lattis.ellipse.domain.interactor.UseCase<AddPrivateNetworkResponse> {

    private UserRepository userRepository;
    private  String email;

    @Inject
    protected AddPrivateNetworkUseCase(ThreadExecutor threadExecutor,
                                       PostExecutionThread postExecutionThread,
                                       UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }
    public AddPrivateNetworkUseCase withEmail(String email) {
        this.email = email;
        return this;
    }


    @Override
    protected Observable<AddPrivateNetworkResponse> buildUseCaseObservable() {
        return userRepository.addPrivateNetworkEmail(email);
    }
}
