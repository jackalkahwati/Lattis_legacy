package com.lattis.ellipse.presentation.ui.authentication.signin;

import androidx.annotation.StringRes;

import com.lattis.ellipse.presentation.ui.base.BaseView;

interface SignInView extends BaseView {

    void showPhoneNumberError(@StringRes int string);

    void hidePhoneNumberError();

    void showPasswordError(@StringRes int string);

    void hidePasswordError();

    void onUserVerified(String email);

    void onUserNotVerified(String email);

    void onAuthenticationFailed();

    void onUserNotExists();

    void openCodeConfirmationScreen();


}
