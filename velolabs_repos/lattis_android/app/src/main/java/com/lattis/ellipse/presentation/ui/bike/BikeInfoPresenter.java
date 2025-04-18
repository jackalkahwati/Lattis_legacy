package com.lattis.ellipse.presentation.ui.bike;


import android.os.Bundle;
import androidx.annotation.Nullable;

import com.lattis.ellipse.data.network.model.response.BasicResponse;
import com.lattis.ellipse.domain.interactor.card.GetCardUseCase;
import com.lattis.ellipse.domain.interactor.card.UpdateCardUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.reactivex.disposables.Disposable;


public class BikeInfoPresenter extends ActivityPresenter<BikeInfoView> {
    private Disposable locationSubscription;
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private Location fromLocation, toLocation;
    private GetCardUseCase getCardUseCase;
    private UpdateCardUseCase updateCardUseCase;


    @Inject
    public BikeInfoPresenter(GetCardUseCase getCardUseCase, UpdateCardUseCase updateCardUseCase,
                             GetLocationUpdatesUseCase getLocationUpdatesUseCase) {
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.getCardUseCase = getCardUseCase;
        this.updateCardUseCase = updateCardUseCase;
    }

    @Override
    protected void updateViewState() {
        getCards();
    }

    @Override
    protected void setup(@Nullable Bundle arguments) {
        super.setup(arguments);
    }

    public void getCards() {

        subscriptions.add(getCardUseCase
                .execute(new RxObserver<List<Card>>() {
                    @Override
                    public void onNext(List<Card> cards) {
                        view.showCardDetailsList(cards);

                    }


                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    @DebugLog
    public void updateCard(int id) {
        subscriptions.add(updateCardUseCase
                .setCardId(id)
                .execute(new RxObserver<BasicResponse>() {
                    @Override
                    public void onNext(BasicResponse o) {
                        super.onNext(o);
                        getCards();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                }));
    }

    public void requestLocationUpdates() {
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {
            @Override
            public void onNext(Location location) {
                fromLocation = location;
                view.setUserPosition(location);
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }


    public void setBikeLocation(Location bikeLocation) {
        this.toLocation = bikeLocation;
    }
}
