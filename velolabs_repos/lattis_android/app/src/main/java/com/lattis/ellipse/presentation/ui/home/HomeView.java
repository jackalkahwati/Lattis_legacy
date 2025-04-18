package com.lattis.ellipse.presentation.ui.home;

import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.presentation.ui.base.BaseView;

interface HomeView extends BaseView {
    void showTermsAndConditions();
    void showOnBoardingFlow();

    void onGetCurrentUserStatusFailure();
    void onGetCurrentUserStatusSuccess(GetCurrentUserStatusResponse getCurrentUserStatusResponse);
}
