package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.data.network.model.mapper.AccountMapper;
import com.lattis.ellipse.data.network.model.response.AuthenticationResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.authentication.base.BaseAuthenticationUseCase;
import com.lattis.ellipse.domain.interactor.error.SignUpValidationError;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.model.VerificationBundle;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.domain.repository.UserRepository;
import com.lattis.ellipse.domain.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static com.lattis.ellipse.domain.interactor.error.SignUpValidationError.Status.INVALID_EMAIL;
import static com.lattis.ellipse.domain.interactor.error.SignUpValidationError.Status.INVALID_PASSWORD;

public class SignUpUseCase extends BaseAuthenticationUseCase {

    private AccountMapper accountMapper;
    private AccountRepository accountRepository;

    @Inject
    public SignUpUseCase(ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread,
                         Authenticator authenticator,
                         AccountRepository accountRepository,
                         UserRepository userRepository,
                         AccountMapper accountMapper
                         ) {
        super(threadExecutor, postExecutionThread,authenticator,accountRepository,userRepository);
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
    }


    public SignUpUseCase withValue(String email,
                                   String firstName,
                                   String lastName,
                                   String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = User.Type.LATTIS.getValue();
        return this;
    }

    @Override
    protected Observable<VerificationBundle> buildUseCaseObservable() {
        return trySignUp();
    }

    private Observable<VerificationBundle> trySignUp(){
        List<SignUpValidationError.Status> errors = getErrors();
        if(errors.isEmpty()){
            return authenticator.signUp(userType,
                    email,
                    password,
                    "fcm_token",firstName,lastName)
                    .flatMap(new Function<AuthenticationResponse, Observable<Account>>() {
                        @Override
                        public Observable<Account> apply(AuthenticationResponse authenticationResponse) {
                            if (authenticationResponse.getUser().isVerified()) {
                                return accountRepository.addAccountExplicitly(accountMapper.mapIn(authenticationResponse.getUser()));
                            }
                            return Observable.just(accountMapper.mapIn(authenticationResponse.getUser()));
                        }
                    }).flatMap(new Function<Account, Observable<VerificationBundle>>() {
                        @Override
                        public Observable<VerificationBundle> apply(Account account) {
                            VerificationBundle verificationBundle = new VerificationBundle();
                            verificationBundle.setUserId(account.getUserId());
                            verificationBundle.setVerified(account.isVerified());
                            return Observable.just(verificationBundle);
                        }
                    });


        } else {
            return Observable.error(new SignUpValidationError(errors));
        }
    }

    private List<SignUpValidationError.Status> getErrors(){
        List<SignUpValidationError.Status> statuses = new ArrayList<>();
        if(isPasswordInvalid()){
            statuses.add(INVALID_PASSWORD);
        }
        if(isEmailInvalid()){
            statuses.add(INVALID_EMAIL);
        }
        return statuses;
    }

    private boolean isEmailInvalid(){
        return ! StringUtils.isLongerThanMinLength(password);
    }
}
