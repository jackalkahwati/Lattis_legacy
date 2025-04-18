package com.lattis.ellipse.presentation.ui.bike.bikeList;

import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by lattis on 20/07/17.
 */

public interface NoServiceView extends BaseView {
    void onGetUserSuccess(User user);
    void onGetUserFail();
}
