package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.data.UserDataRepository;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.ConfirmCodeValidationError;
import com.lattis.ellipse.domain.repository.AccountRepository;
import com.lattis.ellipse.domain.repository.Authenticator;
import com.lattis.ellipse.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.lattis.ellipse.domain.interactor.error.ConfirmCodeValidationError.Status.INVALID_CONFIRMATION_CODE;

/**
 * Created by ssd3 on 5/9/17.
 */

public class ConfirmVerificationForPrivateNetworkUseCase extends UseCase<Boolean> {

    private Authenticator authenticator;
    private AccountRepository accountRepository;
    private UserRepository userRepository;

    private String confirmationCode;
    private String user_id;
    private String account_type;

    @Inject
    public ConfirmVerificationForPrivateNetworkUseCase(ThreadExecutor threadExecutor,
                                            PostExecutionThread postExecutionThread,
                                            UserDataRepository userDataRepository
    ) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userDataRepository;
    }

    public ConfirmVerificationForPrivateNetworkUseCase forUser(String user_id) {
        this.user_id = user_id;
        return this;
    }

    public ConfirmVerificationForPrivateNetworkUseCase forAccountType(String account_type) {
        this.account_type = account_type;
        return this;
    }

    public ConfirmVerificationForPrivateNetworkUseCase withConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        List<ConfirmCodeValidationError.Status> errors = getErrors();
        if (errors.isEmpty()) {
            return userRepository.confirmVerificationCodeForPrivateNetwork(user_id, account_type,confirmationCode);
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
