package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class AcceptTermsConditionsUseCase extends UseCase<Boolean> {

    private UserRepository repository;
    private boolean hasAccepted = false;

    @Inject
    public AcceptTermsConditionsUseCase(ThreadExecutor threadExecutor,
                                        PostExecutionThread postExecutionThread,
                                        UserRepository repository) {
        super(threadExecutor, postExecutionThread);
        this.repository = repository;
    }

    public AcceptTermsConditionsUseCase setHasAccepted(boolean hasAccepted) {
        this.hasAccepted = hasAccepted;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.repository.acceptTermsAndCondition(hasAccepted);
    }

}
