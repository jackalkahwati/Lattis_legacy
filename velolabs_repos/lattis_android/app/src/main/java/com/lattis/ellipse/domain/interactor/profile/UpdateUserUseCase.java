package com.lattis.ellipse.domain.interactor.profile;

import android.text.TextUtils;
import android.util.Patterns;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.interactor.error.UpdateUserValidationError;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdateUserUseCase extends UseCase<User> {

    private UserRepository userRepository;

    private String firstName;
    private String lastName;
    private String email;
    private User user;

    @Inject
    public UpdateUserUseCase(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                             UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public UpdateUserUseCase withUser(User user) {
        this.user = user;
        return this;
    }



    @Override
    protected Observable<User> buildUseCaseObservable() {

        if(user.getEmail()!=null && !user.getEmail().equals("")){
            List<UpdateUserValidationError.Status> errors = getErrors();
            if (errors.isEmpty()) {
                return this.userRepository.saveUser(user);
            } else {
                return Observable.create(subscriber -> {
                    subscriber.onError(new UpdateUserValidationError(errors));
                });
            }
        }else{
            return this.userRepository.saveUser(user);
        }

    }

    private List<UpdateUserValidationError.Status> getErrors() {
        List<UpdateUserValidationError.Status> statuses = new ArrayList<>();
        if (!isEmailValid()) {
            statuses.add(UpdateUserValidationError.Status.INVALID_EMAIL);
        }
        return statuses;
    }

    private boolean isEmailValid() {
        return !TextUtils.isEmpty(user.getEmail()) &&
                Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches();
    }

}
