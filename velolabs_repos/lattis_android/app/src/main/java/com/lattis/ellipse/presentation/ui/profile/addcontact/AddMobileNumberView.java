package com.lattis.ellipse.presentation.ui.profile.addcontact;

import androidx.annotation.StringRes;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by lattis on 29/04/17.
 */

public interface AddMobileNumberView extends BaseView {
    void showPhoneNumberCountryPrefix(int phoneNumberCountryPrefix);
    public void onPhoneNumberUpdated();

    public void showPhoneNumberError(@StringRes int error);
    public void hidePhoneNumberError();

    void onPhoneNumberUpdateFail();

}
