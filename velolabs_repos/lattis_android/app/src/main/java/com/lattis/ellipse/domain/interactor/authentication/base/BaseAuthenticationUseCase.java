package com.lattis.ellipse.domain.interactor.authentication.base;

import com.lattis.ellipse.data.network.model.response.AuthenticationResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.VerificationBundle;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.domain.repository.UserRepository;
import com.lattis.ellipse.domain.utils.StringUtils;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public abstract class BaseAuthenticationUseCase extends UseCase<VerificationBundle> {

    protected Authenticator authenticator;
    private AccountRepository accountRepository;
    private UserRepository userRepository;

    protected String email;
    protected String firstName;
    protected String lastName;
    protected String password;
    protected String userType;

    public BaseAuthenticationUseCase(ThreadExecutor threadExecutor,
                                     PostExecutionThread postExecutionThread,
                                     Authenticator authenticator,
                                     AccountRepository accountRepository,
                                     UserRepository userRepository
                                     ) {
        super(threadExecutor, postExecutionThread);
        this.authenticator = authenticator;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }


    protected boolean isPasswordInvalid(){
        return ! StringUtils.isLongerThanMinLength(password);
    }


    protected Function<AuthenticationResponse, Observable<VerificationBundle>> getAuthenticationBundle = authenticationResponse -> {
        VerificationBundle verificationBundle = new VerificationBundle();
        verificationBundle.setUserId(authenticationResponse.getUser().getUserId());
        verificationBundle.setVerified(authenticationResponse.getUser().isVerified());
        return Observable.just(verificationBundle);
    };
}
