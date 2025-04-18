package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.ConfirmCodeValidationError;
import com.lattis.ellipse.domain.model.Account;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

import static com.lattis.ellipse.domain.interactor.error.ConfirmCodeValidationError.Status.INVALID_CONFIRMATION_CODE;

public class ConfirmVerificationUseCase extends UseCase<User> {

    private Authenticator authenticator;
    private AccountRepository accountRepository;
    private UserRepository userRepository;

    private String confirmationCode;
    private String user_id;
    private String account_type;
    private String password;

    @Inject
    public ConfirmVerificationUseCase(ThreadExecutor threadExecutor,
                                      PostExecutionThread postExecutionThread,
                                      Authenticator authenticator,
                                      UserRepository userRepository,
                                      AccountRepository accountRepository) {
        super(threadExecutor, postExecutionThread);
        this.authenticator = authenticator;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public ConfirmVerificationUseCase forUser(String user_id) {
        this.user_id = user_id;
        return this;
    }

    public ConfirmVerificationUseCase forAccountType(String account_type) {
        this.account_type = account_type;
        return this;
    }

    public ConfirmVerificationUseCase withConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    public ConfirmVerificationUseCase withPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    protected Observable<User> buildUseCaseObservable() {
        List<ConfirmCodeValidationError.Status> errors = getErrors();
        if (errors.isEmpty()) {
            return authenticator.confirmVerificationCode(user_id, account_type,confirmationCode,password)
                    .flatMap(new Function<Account, Observable<Account>>() {
                @Override
                public Observable<Account> apply(Account account) {
                    return accountRepository.addAccountExplicitly(account);
                }
                }).map(new Function<Account, User>() {
                        @Override
                        public User apply(Account account) {
                            //return new User();
                            User user = new User();
                            user.setId(account.getUserId());
                            user.setVerified(account.isVerified());
                            return user;
                        }
                    });
        } else {
            return Observable.error(new ConfirmCodeValidationError(errors));
        }
    }

    private List<ConfirmCodeValidationError.Status> getErrors() {
        List<ConfirmCodeValidationError.Status> statuses = new ArrayList<>();
        if (confirmationCode == null || confirmationCode.length() != 6) {
            statuses.add(INVALID_CONFIRMATION_CODE);
        }
        return statuses;
    }
}
