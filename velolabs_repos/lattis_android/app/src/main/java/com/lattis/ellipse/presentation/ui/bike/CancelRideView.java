package com.lattis.ellipse.presentation.ui.bike;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by Velo Labs Android on 20-04-2017.
 */

public interface CancelRideView extends BaseView
{

        void onCancelBikeSuccess();
        void onCancelBikeFail();

        void onLockDisconnectionSuccess();
        void onLockDisconnectionFail();
}
