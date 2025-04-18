package com.lattis.ellipse.presentation.ui.biketheft;

import android.util.Log;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.data.network.model.response.GetCurrentUserStatusResponse;
import com.lattis.ellipse.domain.interactor.bike.CancelReserveBikeUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.maintenance.ReportTheftUseCase;
import com.lattis.ellipse.domain.interactor.ride.EndRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.updatetrip.StopActiveTripUseCase;
import com.lattis.ellipse.domain.interactor.user.GetCurrentUserStatusUseCase;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * Created by Velo Labs Android on 17-04-2017.
 */

public class ReportBikeTheftPresenter extends ActivityPresenter<ReportBikeView> {
    private final ReportTheftUseCase reportTheftUseCase;
    private final GetRideUseCase getRideUseCase;
    private final StopActiveTripUseCase stopActiveTripUseCase;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private final GetCurrentUserStatusUseCase getCurrentUserStatusUseCase;
    private final CancelReserveBikeUseCase cancelReserveBikeUseCase;
    private final DisconnectAllLockUseCase disconnectAllLockUseCase;
    private final EndRideUseCase endRideUseCase;
    private Disposable locationSubscription;
    private Location currentLocation;
    private final String TAG = ReportBikeTheftPresenter.class.getName();
    private Disposable endRideSubscription;

    @Inject
    ReportBikeTheftPresenter(ReportTheftUseCase reportTheftUseCase,
                             StopActiveTripUseCase stopActiveTripUseCase,
                             GetRideUseCase getRideUseCase,
                             GetCurrentUserStatusUseCase getCurrentUserStatusUseCase,
                             CancelReserveBikeUseCase cancelReserveBikeUseCase,
                             DisconnectAllLockUseCase disconnectAllLockUseCase,
                             GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                             EndRideUseCase endRideUseCase) {
        this.reportTheftUseCase = reportTheftUseCase;
        this.stopActiveTripUseCase = stopActiveTripUseCase;
        this.getRideUseCase = getRideUseCase;
        this.getCurrentUserStatusUseCase = getCurrentUserStatusUseCase;
        this.disconnectAllLockUseCase =disconnectAllLockUseCase;
        this.cancelReserveBikeUseCase = cancelReserveBikeUseCase;
        this.getLocationUpdatesUseCase  = getLocationUpdatesUseCase;
        this.endRideUseCase = endRideUseCase;
    }

    @Override
    protected void updateViewState() {
        getRide();
        getCurrentUserStatus();
    }

    public void reportBikeTheft(int bikeId,int trip_id) {
               subscriptions.add(reportTheftUseCase
                .withBikeId(bikeId)
                       .withTripId(trip_id)
                .execute(new RxObserver<BasicResponse>(view) {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        view.reportTheftSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.reportTheftFailure();
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


    public void getRide() {
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view) {
                    @Override
                    public void onNext(Ride ride) {
                        super.onNext(ride);
                        if(ride==null){
                            view.onRideFailure();
                        }else{
                            view.onRideSuccess(ride);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onRideFailure();
                    }
                }));
    }

    public void getCurrentUserStatus() {
        subscriptions.add(getCurrentUserStatusUseCase
                .execute(new RxObserver<GetCurrentUserStatusResponse>(view) {
                    @Override
                    public void onNext(GetCurrentUserStatusResponse getCurrentUserStatusResponse) {
                        super.onNext(getCurrentUserStatusResponse);
                        if (getCurrentUserStatusResponse.getCurrentUserStatusTripResponse() != null) {
                            view.isRideStarted(true);
                        } else if (getCurrentUserStatusResponse.getCurrentUserActiveBookingStatusResponse() != null) {
                            view.isRideStarted(false);
                        } else {
                            view.isRideStarted(false);
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetCurrentStatusFailure();
                    }
                }));
    }

    public void cancelBikeReservation(int bikeId, boolean isBikeDamage) {
        subscriptions.add(cancelReserveBikeUseCase
                .withBikeId(bikeId)
                .withDamage(isBikeDamage)
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onCancelBikeSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onCancelBikeFail();
                    }
                }));
    }

    public void disconnectAllLocks() {
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onLockDisconnectionSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onLockDisconnectionFail();
                    }
                }));
    }

    public void requestLocationUpdates() {
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                currentLocation=location;
                requestStopLocationUpdates();
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }



    @DebugLog
    public void endRide(int trip_id) {
        Log.e("ReportBikeTheftPres","endRide");
            endRideSubscription = endRideUseCase
                    .withTripId(trip_id)
                    .withLocation(currentLocation)
                    .execute(new RxObserver<Boolean>(view) {
                        @Override
                        public void onNext(Boolean status) {
                            super.onNext(status);
                            if(view!=null){
                                view.onEndRide();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            if(view!=null){
                                view.onEndRide();
                            }
                        }
                    });

    }


}
