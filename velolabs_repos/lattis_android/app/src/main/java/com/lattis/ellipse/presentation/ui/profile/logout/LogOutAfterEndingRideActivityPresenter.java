package com.lattis.ellipse.presentation.ui.profile.logout;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.domain.interactor.authentication.LogOutUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;
import com.lattis.ellipse.presentation.ui.ride.EndRideCheckListActivity;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

/**
 * Created by ssd3 on 5/13/17.
 */

public class LogOutAfterEndingRideActivityPresenter extends ActivityPresenter<LogOutAfterEndingRideActivityView> {



    int trip_id;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private LogOutUseCase logOutUseCase;
    private Disposable locationSubscription;
    private double latitude;
    private double longitude;
    private Location currentUserLocation;

    @Inject
    LogOutAfterEndingRideActivityPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                                           LogOutUseCase logOutUseCase){
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.logOutUseCase = logOutUseCase;
    }

    public Location getCurrentUserLocation() {
        return currentUserLocation;
    }

    public int getTrip_id() {
        return trip_id;
    }

    public void requestLocationUpdates() {
        requestStopLocationUpdates();
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentUserLocation = location;
                requestStopLocationUpdates();
            }
        }));
    }

    public void requestStopLocationUpdates() {

        if(locationSubscription!=null)
            locationSubscription.dispose();
    }

    void logOut() {
        subscriptions.add(logOutUseCase.execute(new RxObserver<Boolean>(view, false) {
            @Override
            public void onNext(Boolean success) {
                view.onLogOutSuccess();
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                view.onLogOutFailure();
            }
        }));
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            trip_id = arguments.getInt(EndRideCheckListActivity.TRIP_ID);
        }
    }



}
