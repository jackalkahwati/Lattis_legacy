package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 3/17/17.
 */

public interface ConfirmCodeForForgotPasswordView extends BaseView {

    void onSecretCodeResent();
    void onSecretCodeSent();
    void onSecretCodeResentFailed();

}
