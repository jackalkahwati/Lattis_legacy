package com.lattis.ellipse.presentation.ui.bike;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.BaseView;

public interface BikeBaseFragmentView extends BaseView {
    void setUserPosition(Location location);
    void OnReserveBikeSuccess(long startTime, int countDownTime);
    void OnReserveBikeFail();
    void OnReserveBikeNotFound();
}
