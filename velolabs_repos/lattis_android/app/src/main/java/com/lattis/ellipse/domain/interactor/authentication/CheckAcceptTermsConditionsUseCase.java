package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by raverat on 2/25/17.
 */

public class CheckAcceptTermsConditionsUseCase extends UseCase<Boolean> {

    private UserRepository repository;

    @Inject
    protected CheckAcceptTermsConditionsUseCase(ThreadExecutor threadExecutor,
                                                PostExecutionThread postExecutionThread,
                                                UserRepository repository) {
        super(threadExecutor, postExecutionThread);
        this.repository = repository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return repository.checkTermsAndConditionAccepted();
    }

}
