package com.lattis.ellipse.presentation.ui.utils;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.ACTIONBTN_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.ACTIONBTN_POP_UP2;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.SUBTITLE1_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.SUBTITLE2_POP_UP;
import static com.lattis.ellipse.presentation.ui.utils.PopUpActivity.TITLE_POP_UP;

/**
 * Created by ssd3 on 4/26/17.
 */

public class PopUpActivity3Presenter extends ActivityPresenter<PopUpActivity3View> {

    private String title=null;
    private String subTitle1=null;
    private String subTitle2=null;
    private String actionBtn1=null;
    private String actionBtn2=null;


    @Inject
    PopUpActivity3Presenter(){

    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            if (arguments.containsKey(TITLE_POP_UP)) {
                title = arguments.getString(TITLE_POP_UP);
            }
            if (arguments.containsKey(SUBTITLE1_POP_UP)) {
                subTitle1 = arguments.getString(SUBTITLE1_POP_UP);
            }
            if (arguments.containsKey(SUBTITLE2_POP_UP)) {
                subTitle2 = arguments.getString(SUBTITLE2_POP_UP);
            }
            if (arguments.containsKey(ACTIONBTN_POP_UP)) {
                actionBtn1 = arguments.getString(ACTIONBTN_POP_UP);
            }

            if (arguments.containsKey(ACTIONBTN_POP_UP2)) {
                actionBtn2 = arguments.getString(ACTIONBTN_POP_UP2);
            }
            view.setView(title,subTitle1,subTitle2,actionBtn1,actionBtn2);
        }
    }

    @Override
    protected void updateViewState() {

    }
}
