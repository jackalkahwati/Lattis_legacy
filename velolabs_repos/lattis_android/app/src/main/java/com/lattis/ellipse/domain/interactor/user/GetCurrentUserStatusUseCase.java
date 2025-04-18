package com.lattis.ellipse.domain.interactor.user;

import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/12/17.
 */

public class GetCurrentUserStatusUseCase extends UseCase<GetCurrentUserStatusResponse> {

    private UserRepository userRepository;


    @Inject
    protected GetCurrentUserStatusUseCase(ThreadExecutor threadExecutor,
                                          PostExecutionThread postExecutionThread,
                                          UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;

    }

    @Override
    protected Observable<GetCurrentUserStatusResponse> buildUseCaseObservable() {
        return userRepository.getCurrentUserStatus();
    }

}
