package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.Authenticator;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GetVerificationCodeUseCase extends UseCase<Boolean> {

    private Authenticator authenticator;
    private String user_id;
    private String account_type;

    @Inject
    public GetVerificationCodeUseCase(ThreadExecutor threadExecutor,
                                      PostExecutionThread postExecutionThread,
                                      Authenticator authenticator) {
        super(threadExecutor, postExecutionThread);
        this.authenticator = authenticator;
    }

    public GetVerificationCodeUseCase forUser(String user_id) {
        this.user_id = user_id;
        return this;
    }

    public GetVerificationCodeUseCase forAccountType(String account_type) {
        this.account_type = account_type;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return authenticator.sendVerificationCode(user_id,account_type);
    }

}
