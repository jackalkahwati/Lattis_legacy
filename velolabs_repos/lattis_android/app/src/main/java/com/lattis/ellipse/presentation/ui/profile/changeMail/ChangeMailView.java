package com.lattis.ellipse.presentation.ui.profile.changeMail;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by lattis on 02/05/17.
 */

public interface ChangeMailView extends BaseView {

    void onCodeSentSuccess(String email);
    void onNoNewFleetWithCurrentFleetPresent();
    void onNoNewFleetWithNoCurrentFleetPresent();
    void onCodeSentFail();
    void onAccountType(String type);
    void setUserId(String userId);
}
