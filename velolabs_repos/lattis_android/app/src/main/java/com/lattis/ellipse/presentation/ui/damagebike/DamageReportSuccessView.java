package com.lattis.ellipse.presentation.ui.damagebike;

import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by lattis on 01/06/17.
 */

public interface DamageReportSuccessView  extends BaseView{
    void setTripID(int tripID);
    void setForceEndRide(boolean isForceEndRide);
}
