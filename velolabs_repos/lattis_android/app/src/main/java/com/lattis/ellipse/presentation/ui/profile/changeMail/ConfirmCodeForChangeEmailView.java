package com.lattis.ellipse.presentation.ui.profile.changeMail;

import androidx.annotation.StringRes;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 3/15/17.
 */

public interface ConfirmCodeForChangeEmailView extends BaseView {
    void onCodeEmailUpdated();
    void onCodeEmailUpdateFail();
    void isPrivateAccount(boolean isPrivate);
    void showConfirmationCodeError(@StringRes int error);
    void hideConfirmationCodeError();


}
