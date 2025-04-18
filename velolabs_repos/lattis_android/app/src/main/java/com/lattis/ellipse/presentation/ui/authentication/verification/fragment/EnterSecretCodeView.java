package com.lattis.ellipse.presentation.ui.authentication.verification.fragment;

import androidx.annotation.StringRes;

import com.lattis.ellipse.presentation.ui.base.BaseView;

public interface EnterSecretCodeView extends BaseView {

    void showConfirmationCodeError(@StringRes int error);
    void hideConfirmationCodeError();

    void onSecretCodeFail();

    void onSecretCodeConfirmed();
    void onSecretCodeResent();

}
