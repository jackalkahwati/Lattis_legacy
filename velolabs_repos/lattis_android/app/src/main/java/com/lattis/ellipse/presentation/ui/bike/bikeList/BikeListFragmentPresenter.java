package com.lattis.ellipse.presentation.ui.bike.bikeList;

import com.lattis.ellipse.domain.interactor.bike.BikeDetailUseCase;
import com.lattis.ellipse.domain.interactor.bike.FindBikesUseCase;
import com.lattis.ellipse.domain.interactor.card.GetCardUseCase;
import com.lattis.ellipse.domain.interactor.map.GetDistanceUseCase;
import com.lattis.ellipse.domain.interactor.user.GetUserUseCase;
import com.lattis.ellipse.domain.model.Bike;
import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.SearchBike;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.setting.IntPref;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.fragment.FragmentPresenter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

import static com.lattis.ellipse.presentation.dagger.module.SettingModule.KEY_QR_CODE_HELP_COUNT;

/**
 * Created by lattis on 24/08/17.
 */

public class BikeListFragmentPresenter extends FragmentPresenter<BikeListView> {

    @Override
    protected void updateViewState() {
        super.updateViewState();
    }

    private FindBikesUseCase findBikesUseCase;
    private GetCardUseCase getCardUseCase;
    private GetDistanceUseCase getDistanceUseCase;
    private GetUserUseCase getUserUseCase;
    private Location currentUserLocation;
    private final BikeDetailUseCase bikeDetailUseCase;
    private final int QR_CODE_HELP_COUNT = 5;
    private IntPref QR_CODE_HELP_COUNT_PREF;
    // TODO it shoud come from fleet
    private boolean isPhoneNumberOK;
    private Disposable getUserSubscription = null;


    @Inject
    BikeListFragmentPresenter(@Named(KEY_QR_CODE_HELP_COUNT) IntPref QR_CODE_HELP_COUNT_PREF, FindBikesUseCase findBikesUseCase,
                              GetCardUseCase getCardUseCase,
                              GetDistanceUseCase getDistanceUseCase,
                              BikeDetailUseCase bikeDetailUseCase,
                              GetUserUseCase getUserUseCase) {
        this.findBikesUseCase = findBikesUseCase;
        this.getCardUseCase = getCardUseCase;
        this.getDistanceUseCase = getDistanceUseCase;
        this.getUserUseCase = getUserUseCase;
        this.bikeDetailUseCase = bikeDetailUseCase;
        this.QR_CODE_HELP_COUNT_PREF = QR_CODE_HELP_COUNT_PREF;
    }


    public void getDistanceForNearestBikes(List<Bike> bikes) {
        subscriptions.add(getDistanceUseCase
                .forBikes(bikes)
                .from(currentUserLocation)
                .execute(new RxObserver<List<Bike>>() {
                    @Override
                    public void onNext(List<Bike> bikes) {
                        if(view!=null)
                            view.onFindNearBikesSuccess(bikes);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onFindBikeFailure();
                    }
                }));
    }

    public void getDistanceForAvailableBikes(List<Bike> bikes) {
        subscriptions.add(getDistanceUseCase
                .forBikes(bikes)
                .from(currentUserLocation)
                .execute(new RxObserver<List<Bike>>() {
                    @Override
                    public void onNext(List<Bike> bikes) {
                        if(view!=null)
                            view.onFindAvailableBikeSuccess(bikes);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.onFindBikeFailure();
                    }
                }));
    }


    public void findBikes(Location location) {
        this.currentUserLocation = location;
        subscriptions.add(findBikesUseCase
                .withLatitude(currentUserLocation.getLatitude())
                .withLongitude(currentUserLocation.getLongitude())
                .execute(new RxObserver<SearchBike>() {
                    @Override
                    public void onNext(SearchBike bikes) {
                        if (QR_CODE_HELP_COUNT_PREF.getValue() < QR_CODE_HELP_COUNT) {
                            if(view!=null)
                                view.showToolTip();
                        }

                        getCards();
                        if(view!=null)
                            view.hideOperatingLoading();
                        if (bikes != null && bikes.getNearestBikeList().size() > 0) {
                            getDistanceForNearestBikes(bikes.getNearestBikeList());
                        } else if (bikes != null && bikes.getAvailableBikeList().size() > 0) {
                            getDistanceForAvailableBikes(bikes.getAvailableBikeList());
                        } else if (bikes != null && bikes.getAvailableBikeList().size() == 0 && bikes.getNearestBikeList().size() == 0) {
                            if(view!=null)
                                view.onFindBikesEmpty();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null)
                            view.hideOperatingLoading();
                        if (e!=null && e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == 404) {
                                if(view!=null)
                                    view.onFindBikeNoServiceAvailable();
                            } else {
                                if(view!=null)
                                    view.onFindBikeFailure();
                            }
                        } else {
                            if(view!=null)
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


    public void getUserProfile(){
        isPhoneNumberOK=false;
        cancelGetUserSubscription();
        getUserSubscription = getUserUseCase.execute(new RxObserver<User>(view, false) {
            @Override
            public void onNext(User currUser) {
                if(currUser!=null){
                    isPhoneNumberOK = (currUser.getPhoneNumber()!=null && !currUser.getPhoneNumber().equals("")) ? true : false;
                }
                if(view!=null)
                    view.onGetUserProfile();
            }
            @Override
            public void onError(Throwable e) {
                super.onError(e);
                if(view!=null)
                    view.onGetUserProfile();
            }
        });
    }


    public boolean phoneNumberCheckPassed(Bike bike){
        return bike.isRequire_phone_number() ? isPhoneNumberOK : true;
    }

    public void cancelGetUserSubscription(){
        if(getUserSubscription!=null){
            getUserSubscription.dispose();
            getUserSubscription=null;
        }
    }


}
