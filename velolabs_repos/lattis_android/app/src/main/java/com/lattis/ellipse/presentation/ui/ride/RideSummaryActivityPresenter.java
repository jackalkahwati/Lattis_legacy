package com.lattis.ellipse.presentation.ui.ride;

import com.lattis.ellipse.data.network.model.response.ride.RideSummaryResponse;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.interactor.ride.GetRideUseCase;
import com.lattis.ellipse.domain.interactor.ride.RideRatingUseCase;
import com.lattis.ellipse.domain.interactor.ride.RideSummaryUseCase;
import com.lattis.ellipse.domain.model.Ride;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.exception.BluetoothException;

/**
 * Created by ssd3 on 7/18/17.
 */

public class RideSummaryActivityPresenter extends ActivityPresenter<RideSummaryActivityView> {

    private final GetRideUseCase getRideUseCase;
    private final RideSummaryUseCase rideSummaryUseCase;
    private final RideRatingUseCase rideRatingUseCase;
    private final DisconnectAllLockUseCase disconnectAllLockUseCase;



    @Inject
    RideSummaryActivityPresenter(GetRideUseCase getRideUseCase,RideSummaryUseCase rideSummaryUseCase,
                                 RideRatingUseCase rideRatingUseCase,
                                 DisconnectAllLockUseCase disconnectAllLockUseCase) {
        this.getRideUseCase = getRideUseCase;
        this.rideSummaryUseCase = rideSummaryUseCase;
        this.rideRatingUseCase = rideRatingUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;

    }

    @Override
    protected void updateViewState() {

    }

    public void getRide(){
        subscriptions.add(getRideUseCase
                .execute(new RxObserver<Ride>(view){
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

    public void getRideSummary(int trip_id){
        subscriptions.add(rideSummaryUseCase
                .withTripId(trip_id)
                .execute(new RxObserver<RideSummaryResponse>(view){
                    @Override
                    public void onNext(RideSummaryResponse rideSummaryResponse) {
                        super.onNext(rideSummaryResponse);
                        view.onGetRideSummarySuccess(rideSummaryResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetRideSummaryFailure();
                    }
                }));
    }

    public void disconnectAllLocks(){
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view){
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if(throwable instanceof BluetoothException){
                            BluetoothException exception = (BluetoothException) throwable;
                            if(exception.getStatus().equals(BluetoothException.Status.BLUETOOTH_DISABLED)){

                            }
                        }else{

                        }
                    }
                }));
    }

    public void rideRating(int trip_id,int rating){
        subscriptions.add(rideRatingUseCase
                .withTripId(trip_id)
                .withRating(rating)
                .execute(new RxObserver<Boolean>(view){
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onRideRatingSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onRideRatingFailure();
                    }
                }));
    }




}
