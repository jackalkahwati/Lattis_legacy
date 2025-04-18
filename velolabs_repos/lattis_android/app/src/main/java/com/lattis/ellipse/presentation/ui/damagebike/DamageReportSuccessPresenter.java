package com.lattis.ellipse.presentation.ui.damagebike;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

/**
 * Created by lattis on 01/06/17.
 */

public class DamageReportSuccessPresenter extends ActivityPresenter<DamageReportSuccessView> {
    private final GetRideUseCase getRideUseCase;

    @Inject
    DamageReportSuccessPresenter(GetRideUseCase getRideUseCase) {
        this.getRideUseCase = getRideUseCase;
    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
        getRide();
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null && arguments.get("TRIP_ID") != null) {
            view.setTripID(arguments.getInt("TRIP_ID"));
        }
    }

    public void getRide() {
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        if (ride != null)
                            view.setForceEndRide(ride.getBike_skip_parking_image());

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                    }
                }));
    }

}
