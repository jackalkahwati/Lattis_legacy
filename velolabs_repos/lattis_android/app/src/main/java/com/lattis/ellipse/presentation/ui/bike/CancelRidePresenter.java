package com.lattis.ellipse.presentation.ui.bike;


import com.lattis.ellipse.domain.interactor.bike.CancelReserveBikeUseCase;
import com.lattis.ellipse.domain.interactor.lock.disconnect.DisconnectAllLockUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

/**
 * Created by Velo Labs Android on 20-04-2017.
 */

public class CancelRidePresenter extends ActivityPresenter<CancelRideView> {
    CancelReserveBikeUseCase cancelReserveBikeUseCase;
    DisconnectAllLockUseCase disconnectAllLockUseCase;

    private final String TAG = CancelRidePresenter.class.getName();
    @Inject
    CancelRidePresenter(CancelReserveBikeUseCase cancelReserveBikeUseCase, DisconnectAllLockUseCase disconnectAllLockUseCase) {
        this.cancelReserveBikeUseCase = cancelReserveBikeUseCase;
        this.disconnectAllLockUseCase = disconnectAllLockUseCase;
    }

    @Override
    protected void updateViewState() {

    }

    public void cancelBikeReservation(Bike bike, boolean isBikeDamage, boolean lockIssue) {
        subscriptions.add(cancelReserveBikeUseCase
                .withBikeId(bike.getBike_id())
                .withDamage(isBikeDamage)
                .withLockIssue(lockIssue)
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        if(view!=null) {
                            view.onCancelBikeSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null) {
                            view.onCancelBikeFail();
                        }
                    }
                }));
    }

    public void disconnectAllLocks() {
        subscriptions.add(disconnectAllLockUseCase
                .execute(new RxObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        if(view!=null) {
                            view.onLockDisconnectionSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null) {
                            view.onLockDisconnectionFail();
                        }
                    }
                }));
    }


}
