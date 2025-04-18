package com.lattis.ellipse.presentation.ui.bike;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;

import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingZoneUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingsForFleetUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

/**
 * Created by lattis on 24/05/17.
 */

public class FleetParkingZonePresenter extends ActivityPresenter<FleetParkingView> {
    private int fleetId=-1;
    private GetParkingZoneUseCase getParkingZoneUseCase;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private GetParkingsForFleetUseCase getParkingsForFleetUseCase;
    private Disposable locationSubscription = null;
    private Location currentUserLocation;
    public static String ARG_FLEET_ID="ARG_FLEET_ID";


    @Inject
    FleetParkingZonePresenter(GetParkingZoneUseCase getParkingZoneUseCase,
                              GetParkingsForFleetUseCase getParkingsForFleetUseCase,
                              GetLocationUpdatesUseCase getLocationUpdatesUseCase) {
        this.getParkingZoneUseCase = getParkingZoneUseCase;
        this.getParkingsForFleetUseCase = getParkingsForFleetUseCase;
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }


    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            if (arguments.containsKey(ARG_FLEET_ID)) {
                fleetId = arguments.getInt(ARG_FLEET_ID);
            }
        }
    }


    public void getParkingZone() {
        Log.e("FleetParkingZonePresen","getParkingZone::Fleet_ID::"+fleetId);
        subscriptions.add(getParkingZoneUseCase.withFleetID(fleetId)
                .execute(new RxObserver<List<ParkingZone>>() {
                    @Override
                    public void onNext(List<ParkingZone> parkingZone) {
                        if(parkingZone!=null){
                            view.onFindingZoneSuccess(parkingZone);
                        }

                        findParkings();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        findParkings();
                    }
                }));

    }

    public void findParkings() {
        subscriptions.add(getParkingsForFleetUseCase
                .withFleetId(fleetId)
                .execute(new RxObserver<List<Parking>>() {
                    @Override
                    public void onNext(List<Parking> parkings) {
                        if (parkings != null) {
                            view.onFindingParkingSuccess(parkings);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onFindingParkingFailure();
                    }
                }));
    }

    public int getFleetId() {
        return fleetId;
    }


    public void requestLocationUpdates() {
        requestStopLocationUpdates();
        locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentUserLocation = location;
                if(view!=null)
                    view.setUserPosition(location);
            }
        });
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null){
            locationSubscription.dispose();
            locationSubscription =null;
        }

    }

    public Location getCurrentUserLocation() {
        return currentUserLocation;
    }

    public void setCurrentUserLocation(Location currentUserLocation) {
        this.currentUserLocation = currentUserLocation;
    }
}
