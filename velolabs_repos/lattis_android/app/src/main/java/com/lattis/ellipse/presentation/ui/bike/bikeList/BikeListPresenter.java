package com.lattis.ellipse.presentation.ui.bike.bikeList;


import com.lattis.ellipse.domain.interactor.bike.FindBikesUseCase;
import com.lattis.ellipse.domain.interactor.card.GetCardUseCase;
import com.lattis.ellipse.domain.interactor.location.GetLocationUpdatesUseCase;
import com.lattis.ellipse.domain.interactor.map.GetDistanceUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.SearchBike;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

public class BikeListPresenter extends ActivityPresenter<BikeListView> {
    private final GetLocationUpdatesUseCase getLocationUpdatesUseCase;
    private Disposable locationSubscription;
    private FindBikesUseCase findBikesUseCase;
    private GetCardUseCase getCardUseCase;
    private GetDistanceUseCase getDistanceUseCase;
    private Location currentUserLocation;



    @Inject
    BikeListPresenter(GetLocationUpdatesUseCase getLocationUpdatesUseCase,
                      FindBikesUseCase findBikesUseCase,
                      GetCardUseCase getCardUseCase,
                      GetDistanceUseCase getDistanceUseCase
                      ) {
        this.getLocationUpdatesUseCase = getLocationUpdatesUseCase;
        this.findBikesUseCase = findBikesUseCase;
        this.getCardUseCase = getCardUseCase;
        this.getDistanceUseCase = getDistanceUseCase;

    }

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }

    public void requestLocationUpdates() {
        subscriptions.add(locationSubscription = getLocationUpdatesUseCase.execute(new RxObserver<Location>() {

            @Override
            public void onNext(Location location) {
                view.setUserPosition(location);
            }
        }));
    }

    public void requestStopLocationUpdates() {
        if (locationSubscription != null)
            locationSubscription.dispose();
    }



    public void getDistanceForNearestBikes(List<Bike>bikes ) {
        subscriptions.add(getDistanceUseCase
                .forBikes(bikes)
                .from(currentUserLocation)
                .execute(new RxObserver<List<Bike>>() {
                    @Override
                    public void onNext(List<Bike>bikes) {
                        view.onFindNearBikesSuccess(bikes);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onFindBikeFailure();
                    }
                }));
    }

    public void getDistanceForAvailableBikes(List<Bike>bikes ) {
        subscriptions.add(getDistanceUseCase
                .forBikes(bikes)
                .from(currentUserLocation)
                .execute(new RxObserver<List<Bike>>() {
                    @Override
                    public void onNext(List<Bike>bikes) {
                        view.onFindAvailableBikeSuccess(bikes);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onFindBikeFailure();
                    }
                }));
    }



    public void findBikes() {
        subscriptions.add(findBikesUseCase
                .withLatitude(currentUserLocation.getLatitude())
                .withLongitude(currentUserLocation.getLongitude())
                .execute(new RxObserver<SearchBike>() {
                    @Override
                    public void onNext(SearchBike bikes) {
                        if (bikes != null && bikes.getNearestBikeList().size() > 0) {
                            getDistanceForNearestBikes(bikes.getNearestBikeList());
                        } else if (bikes != null && bikes.getAvailableBikeList().size() > 0) {
                            getDistanceForAvailableBikes(bikes.getAvailableBikeList());
                        } else if (bikes != null && bikes.getNearestBikeList().size() == 0 && bikes.getAvailableBikeList().size() == 0) {
                            view.onFindBikesEmpty();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 404) {
                                view.onFindBikeNoServiceAvailable();
                            }else{
                                view.onFindBikeFailure();
                            }
                        }else{
                            view.onFindBikeFailure();
                        }

                    }
                }));
    }

    public void getCards() {
        subscriptions.add(getCardUseCase
                .execute(new RxObserver<List<Card>>() {
                    @Override
                    public void onNext(List<Card> cards) {
                        view.onGetCardSuccess(cards);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onGetCardFailure();
                    }
                }));
    }


    public void setUserLocation(Location userLocation) {
        this.currentUserLocation = userLocation;
    }






}
