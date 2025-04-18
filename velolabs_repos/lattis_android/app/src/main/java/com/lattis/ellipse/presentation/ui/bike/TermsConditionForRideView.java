package com.lattis.ellipse.presentation.ui.bike;


import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.presentation.ui.base.BaseView;

public interface TermsConditionForRideView extends BaseView {

    void onTermsAndConditionsLoaded(TermsAndConditions termsAndConditions);
    void onTermsAndConditionsFailedLoading();
}
