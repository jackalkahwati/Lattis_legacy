package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.domain.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GetTermsConditionsUseCase extends UseCase<TermsAndConditions> {

    private UserRepository repository;;

    @Inject
    public GetTermsConditionsUseCase(ThreadExecutor threadExecutor,
                                     PostExecutionThread postExecutionThread,
                                     UserRepository repository) {
        super(threadExecutor, postExecutionThread);
        this.repository = repository;
    }

    @Override
    protected Observable<TermsAndConditions> buildUseCaseObservable() {
        //TODO save it in db ?
        return this.repository.getTermsAndConditions();
    }

}
