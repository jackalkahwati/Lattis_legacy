package com.lattis.ellipse.presentation.ui.bike;

import android.util.Log;

import com.lattis.ellipse.domain.interactor.bike.ReserveBikeUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

class BikeBaseFragmentPresenter extends FragmentPresenter<BikeBaseFragmentView> {

    private static final String TAG = BikeBaseFragmentPresenter.class.getName();
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private Location currentUserLocation;
    private Disposable locationSubscription = null;
    private ReserveBikeUseCase reserveBikeUseCase;

    @Inject
    public BikeBaseFragmentPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,ReserveBikeUseCase reserveBikeUseCase) {
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.reserveBikeUseCase = reserveBikeUseCase;
    }

    public Location getCurrentUserLocation() {
        return currentUserLocation; //uncomment for real case
    }

    public void resetCurrentUserLocation() {
        currentUserLocation = null;
    }

    public void requestLocationUpdates() {
        Log.e(TAG,"requestLocationUpdates::------->");
        requestStopLocationUpdates();
        locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                Log.e(TAG,"requestLocationUpdates::------->onNext");
                requestStopLocationUpdates();
                if (location != null && view!=null) {
                    currentUserLocation = location;
                    if(view!=null) {
                        view.setUserPosition(location);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Log.e(TAG,"requestLocationUpdates::Error------->");
            }
        });
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }

    @Override
    protected void updateViewState() {

    }
    public void reserveBike(Bike bike) {
        subscriptions.add(reserveBikeUseCase
                .withBike(bike)
                .withScanStatus(false)
                .withLatitude(currentUserLocation.getLatitude())
                .withLongitude(currentUserLocation.getLongitude())
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        Log.e(TAG,"Reserve bike response::"+ride.toString());
                        view.OnReserveBikeSuccess(ride.getBike_booked_on(), ride.getBike_expires_in());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 404) {
                                view.OnReserveBikeNotFound();
                            } else {
                                view.OnReserveBikeFail();
                            }
                        } else {
                            view.OnReserveBikeFail();
                        }
                    }
                }));
    }



}
