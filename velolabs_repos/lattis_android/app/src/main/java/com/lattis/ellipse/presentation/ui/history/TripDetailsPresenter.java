package com.lattis.ellipse.presentation.ui.history;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.lattis.ellipse.data.network.model.response.history.RideHistoryDataResponse;
import com.lattis.ellipse.domain.interactor.card.GetCardUseCase;
import com.lattis.ellipse.domain.interactor.map.GetMapBoxRouteMatcherUseCase;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by lattis on 18/08/17.
 */

public class TripDetailsPresenter extends ActivityPresenter<TripDetailsView> {
    private RideHistoryDataResponse rideHistoryDataResponse;
    GetMapBoxRouteMatcherUseCase getMapBoxRouteMatcherUseCase;
    GetCardUseCase getCardUseCase;

    @Inject
    TripDetailsPresenter(GetMapBoxRouteMatcherUseCase getMapBoxRouteMatcherUseCase,GetCardUseCase getCardUseCase) {
        this.getMapBoxRouteMatcherUseCase = getMapBoxRouteMatcherUseCase;
        this.getCardUseCase = getCardUseCase;
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
        if (arguments != null) {
            if (arguments.containsKey("TRIP_DETAILS")) {
                this.rideHistoryDataResponse = new Gson().fromJson(arguments.getString("TRIP_DETAILS")
                        , RideHistoryDataResponse.class);
                view.setTripDetails(rideHistoryDataResponse);
                getCards();
            }
        }
    }

    public void getCards() {
        subscriptions.add(getCardUseCase
                .execute(new RxObserver<List<Card>>() {
                    @Override
                    public void onNext(List<Card> cards) {
                        if(view!=null)
                            view.onGetCardSuccess(cards);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onGetCardFailure();
                    }
                }));
    }

    public RideHistoryDataResponse getRideHistoryDataResponse() {
        return rideHistoryDataResponse;
    }

}
