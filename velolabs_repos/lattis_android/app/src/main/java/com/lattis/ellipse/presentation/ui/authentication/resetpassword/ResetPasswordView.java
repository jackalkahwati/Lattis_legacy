package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import com.lattis.ellipse.presentation.ui.base.BaseView;

interface ResetPasswordView extends BaseView {
        void onSendForgotPasswordCodeSuccess(String email);
        void onSendForgotPasswordCodeFailure();
}
