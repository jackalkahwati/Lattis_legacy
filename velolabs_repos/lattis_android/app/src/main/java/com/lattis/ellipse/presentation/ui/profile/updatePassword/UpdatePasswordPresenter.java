package com.lattis.ellipse.presentation.ui.profile.updatePassword;

import com.lattis.ellipse.domain.interactor.user.UpdatePasswordUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

/**
 * Created by lattis on 01/05/17.
 */

public class UpdatePasswordPresenter extends ActivityPresenter<UpdatePasswordView> {
    private String password;
    private String newPassword;
    private String repeatPassword;
    UpdatePasswordUseCase updatePasswordUseCase;

    @Inject
    UpdatePasswordPresenter(UpdatePasswordUseCase updatePasswordUseCase) {
        this.updatePasswordUseCase = updatePasswordUseCase;
    }

    void upatePassword()
    {
        if (newPassword.equals(repeatPassword)) {
            subscriptions.add(updatePasswordUseCase
                    .withPassword(password)
                    .withNewPassword(newPassword).execute(new RxObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean status) {
                            super.onNext(status);
                            view.passwordUpdateSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            view.showInCorrectPassword();
                        }
                    }));
        }
        else
            view.showInCorrectPassword();
    }

    public void setPassword(String password) {this.password = password;}
    public void setNewPassword(String newPassword) {this.newPassword = newPassword;}

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
