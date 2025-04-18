package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.SignInValidationError;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.domain.repository.UserRepository;
import com.lattis.ellipse.domain.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static com.lattis.ellipse.domain.interactor.error.SignInValidationError.Status.INVALID_PASSWORD;

public class SignInUseCase extends UseCase<User> {

    private Authenticator authenticator;
    private AccountRepository accountRepository;
    private UserRepository userRepository;

    private String email;
    private String password;
    private String userType;


    @Inject
    public SignInUseCase(ThreadExecutor threadExecutor,
                  PostExecutionThread postExecutionThread,
                  Authenticator authenticator,
                  AccountRepository accountRepository,
                  UserRepository userRepository) {

        super(threadExecutor, postExecutionThread);
        this.authenticator = authenticator;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public SignInUseCase withValues(String email,
                                    String password) {
        this.email = email;
        this.password = password;
        this.userType = User.Type.LATTIS.getValue();
        return this;
    }

    @Override
    protected Observable<User> buildUseCaseObservable() {
        return trySignIn();
    }

    protected Observable<User> trySignIn(){
        List<SignInValidationError.Status> errors = getErrors();
        if(errors.isEmpty()){
            return authenticator.signIn(userType,
                    email,
                    password,
                    "fcm_token")
                    .flatMap(new Function<Account, Observable<Account>>() {
                        @Override
                        public Observable<Account> apply(Account account) {
                            if (account.isVerified()) {
                                return accountRepository.addAccountExplicitly(account);
                            }
                            return Observable.just(account);
                        }
                    }).flatMap(new Function<Account, Observable<User>>() {
                        @Override
                        public Observable<User> apply(Account account) {
                            User user = new User();
                            user.setId(account.getUserId());
                            user.setVerified(account.isVerified());
                            return Observable.just(user);
                        }
                    });
        } else {
            return Observable.error(new SignInValidationError(errors));
        }
    }

    private List<SignInValidationError.Status> getErrors(){
        List<SignInValidationError.Status> statuses = new ArrayList<>();

        if(isPasswordInvalid()){
            statuses.add(INVALID_PASSWORD);
        }
        return statuses;
    }

    protected boolean isPasswordInvalid(){
        return ! StringUtils.isLongerThanMinLength(password);
    }
}