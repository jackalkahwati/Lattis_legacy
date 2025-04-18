package com.lattis.ellipse.presentation.ui.history;

import com.lattis.ellipse.data.network.model.response.history.RideHistoryResponse;
import com.lattis.ellipse.domain.interactor.history.GetRideHistorUseCase;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

/**
 * Created by ssd3 on 8/16/17.
 */

public class RideHistoryListingActivityPresenter extends ActivityPresenter<RideHistoryListingActivityView> {

    private final GetRideHistorUseCase getRideHistorUseCase;

    @Inject
    RideHistoryListingActivityPresenter(GetRideHistorUseCase getRideHistorUseCase) {
        this.getRideHistorUseCase = getRideHistorUseCase;
    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
        getRideHistory();
    }

    @DebugLog
    public void getRideHistory() {
        view.showOperationLoading();
        subscriptions.add(getRideHistorUseCase
                .execute(new RxObserver<RideHistoryResponse>() {
                    @Override
                    public void onNext(RideHistoryResponse rideHistoryResponse) {
                        if (rideHistoryResponse != null) {
                            if (rideHistoryResponse.getRideHistoryDataResponse() != null) {
                                view.onRideHistorySuccess(rideHistoryResponse.getRideHistoryDataResponse());
                            } else {
                                view.onNoRideHistory();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onRideHistoryFailure();

                    }
                }));
    }


}
