package com.lattis.ellipse.presentation.ui.profile;

import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by raverat on 2/20/17.
 */

public interface TermsAndConditionsFragmentView extends BaseView {

    void onTermsAndConditionsLoaded(TermsAndConditions termsAndConditions);
    void onTermsAndConditionsFailedLoading();

}
