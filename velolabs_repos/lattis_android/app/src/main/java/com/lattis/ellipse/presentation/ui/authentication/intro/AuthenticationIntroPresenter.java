package com.lattis.ellipse.presentation.ui.authentication.intro;

import com.lattis.ellipse.presentation.setting.BooleanPref;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;
import javax.inject.Named;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_FIRST_TIME_WALK_THROUGH_STRING;

class AuthenticationIntroPresenter extends ActivityPresenter<AuthenticationIntroView> {

    private BooleanPref firstTimeWalkThroughPref;

    @Inject
    public AuthenticationIntroPresenter(
            @Named(KEY_FIRST_TIME_WALK_THROUGH_STRING) BooleanPref firstTimeWalkThroughPref) {
        this.firstTimeWalkThroughPref = firstTimeWalkThroughPref;
    }

    @Override
    protected void updateViewState() {
        if (firstTimeWalkThroughPref.getValue() == Boolean.TRUE) {
            //view.showWalkThrough();
        }
    }
}
