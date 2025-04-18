package com.lattis.ellipse.presentation.ui.damagebike;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by Velo Labs Android on 03-04-2017.
 */

public interface DamageBikeView extends BaseView {
    void uploadImageSuccess(String imageUrl);
    void uploadImageFail();
    void updateSpinner();
    void selectCategory();
    void setBikeId(int bikeId);
    void setTripID(int tripID);
    void showLoading(String message);
    void hideProgressLoading();
    void showBikeNotYetBooked();
    void damageReportSuccess();
    void damageReportFailure();
    void  isRideStarted(boolean started);
}
