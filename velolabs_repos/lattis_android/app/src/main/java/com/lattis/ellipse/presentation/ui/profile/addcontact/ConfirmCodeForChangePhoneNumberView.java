package com.lattis.ellipse.presentation.ui.profile.addcontact;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by ssd3 on 3/15/17.
 */

public interface ConfirmCodeForChangePhoneNumberView extends BaseView {
    public void onCodePhoneNumberUpdated();
    public void updateVerificationInfoText(String text);
    public void onSendSuccessful();

}
