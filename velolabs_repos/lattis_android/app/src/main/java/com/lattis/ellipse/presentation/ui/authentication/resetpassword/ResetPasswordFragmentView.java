package com.lattis.ellipse.presentation.ui.authentication.resetpassword;

import androidx.annotation.StringRes;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by raverat on 2/22/17.
 */

public interface ResetPasswordFragmentView extends BaseView {

    void showPasswordError(@StringRes int error);
    void hidePasswordError();

    void showFailToSaveDialog();

    void onPasswordChanged();
}
