package com.lattis.ellipse.domain.interactor.authentication;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.ResetPasswordValidationError;
import com.lattis.ellipse.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.lattis.ellipse.domain.interactor.error.ResetPasswordValidationError.Status.INVALID_PASSWORD;

public class ResetPasswordUseCase extends UseCase<BasicResponse> {

    private String email;
    private String confirmation_code;
    private String password;
    private UserRepository userRepository;

    @Inject
    protected ResetPasswordUseCase(ThreadExecutor threadExecutor,
                                   PostExecutionThread postExecutionThread,
                                   UserRepository userRepository
                                   ) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public ResetPasswordUseCase withValues(String email, String confirmation_code, String password) {
        this.email = email;
        this.confirmation_code = confirmation_code;
        this.password = password;
        return this;
    }

    @Override
    protected Observable<BasicResponse> buildUseCaseObservable() {
        List<ResetPasswordValidationError.Status> errors = getErrors();
        if (errors.isEmpty()) {
            return userRepository.confirmCodeForForgotPassword(email,confirmation_code,password);
        } else {
            return Observable.create(subscriber -> {
                subscriber.onError(new ResetPasswordValidationError(errors));
            });
        }
    }

    private List<ResetPasswordValidationError.Status> getErrors() {
        List<ResetPasswordValidationError.Status> statuses = new ArrayList<>();
        if (!isPasswordValid()) {
            statuses.add(INVALID_PASSWORD);
        }
        return statuses;
    }

    private boolean isPasswordValid() {
        if (password == null || !(password.length() >= 8 && password.length() <= 20)) {
            return false;
        }
        return true;
    }

}
