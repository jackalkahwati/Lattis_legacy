package com.lattis.ellipse.presentation.ui.authentication.signup;

import androidx.annotation.StringRes;

import com.lattis.ellipse.presentation.ui.base.BaseView;

interface SignUpView  extends BaseView{

    void showPhoneNumberError(@StringRes int string);

    void hidePhoneNumberError();

    void showPasswordError(@StringRes int string);

    void hidePasswordError();

    void showEmailError(@StringRes int string);

    void hideEmailError();

    void showHomePage(String userId);

    void showRequestCode(String userId);

    void showDuplicateError();

    void onRegistrationFailed();


}
