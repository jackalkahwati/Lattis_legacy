package com.lattis.ellipse.presentation.ui.ride;

import com.lattis.ellipse.Utils.InsideBoundaryUtils;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingZoneUseCase;
import com.lattis.ellipse.domain.interactor.parking.GetParkingsForFleetUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.model.mapper.BikeModelMapper;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;


public class EndRideFragmentPresenter extends FragmentPresenter<EndRideFragmentView> {

    private final String TAG = EndRideFragmentPresenter.class.getName();
    private final GetRideUseCase getRideUseCase;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private final StopActiveTripUseCase stopActiveTripUseCase;
    private Location currentUserLocation;
    private GetParkingZoneUseCase getParkingZoneUseCase;
    private Disposable locationSubscription;
    private double latitude;
    private double longitude;
    private GetParkingsForFleetUseCase getParkingsForFleetUseCase;
    private BikeModelMapper bikeModelMapper;


    @Inject
    EndRideFragmentPresenter(GetRideUseCase getRideUseCase, GetParkingsForFleetUseCase getParkingsForFleetUseCase,
                             GetParkingZoneUseCase getParkingZoneUseCase,
                             GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                             StopActiveTripUseCase stopActiveTripUseCase,
                                     BikeModelMapper bikeModelMapper) {
        this.getRideUseCase = getRideUseCase;
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.getParkingZoneUseCase = getParkingZoneUseCase;
        this.getParkingsForFleetUseCase = getParkingsForFleetUseCase;
        this.stopActiveTripUseCase = stopActiveTripUseCase;
        this.bikeModelMapper = bikeModelMapper;
    }

    @Override
    protected void updateViewState() {

    }

    public void requestLocationUpdates() {
        requestStopLocationUpdates();
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentUserLocation = location;
                if(view!=null) {
                    view.setUserPosition(location);
                }
            }
        }));
    }

    public void getParkingZone(int fleetId) {

        subscriptions.add(getParkingZoneUseCase.withFleetID(fleetId)
                .execute(new RxObserver<List<ParkingZone>>() {
                    @Override
                    public void onNext(List<ParkingZone> parkingZone) {
                        view.onGetParkingZone(parkingZone);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));

    }


    public void bikeInsideZoneOrNot(List<ParkingZone> parkingZone) {
        if (currentUserLocation != null) {
            if (InsideBoundaryUtils.checkPositonWithinBoundaries(
                    new LatLng(currentUserLocation.getLatitude()
                            , currentUserLocation.getLongitude()), parkingZone)) {
                view.onBikeWithInBoundary();
            } else {
                view.onBikeWithOutBoundary();
            }
        }
    }

    public void requestStopLocationUpdates() {

        if (locationSubscription != null)
            locationSubscription.dispose();
    }

    public void setLatitudeLongitude(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void getRide() {
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        view.onGetRideSuccess(ride);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetRideFailure();
                    }
                }));
    }


    public void stopUpdateTripService() {
        subscriptions.add(stopActiveTripUseCase

                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean aVoid) {
                        super.onNext(aVoid);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public BikeModelMapper getBikeModelMapper() {
        return bikeModelMapper;
    }
}
