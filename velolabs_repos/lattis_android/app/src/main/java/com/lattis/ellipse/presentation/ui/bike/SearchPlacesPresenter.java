package com.lattis.ellipse.presentation.ui.bike;
import com.google.android.libraries.places.api.model.Place;
import com.lattis.ellipse.domain.interactor.SavedAddress.GetSavedAddressUseCase;
import com.lattis.ellipse.domain.interactor.SavedAddress.SavedAddressUseCase;
import com.lattis.ellipse.domain.interactor.location.GetPlaceBufferUseCase;
import com.lattis.ellipse.domain.interactor.location.GetPlacesUseCase;
import com.lattis.ellipse.domain.model.SavedAddress;
import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;
import com.lattis.ellipse.presentation.ui.base.RxObserver;
import com.lattis.ellipse.presentation.ui.base.activity.ActivityPresenter;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lattis on 28/08/17.
 */

public class SearchPlacesPresenter extends ActivityPresenter<SearchPlacesView>{
    private final SavedAddressUseCase savedAddressUseCase;
    private final GetSavedAddressUseCase getSavedAddressUseCase;
    private final GetPlacesUseCase getPlacesUseCase;
    private final GetPlaceBufferUseCase getPlaceBufferUseCase;
    private MapboxGeocoding mapboxGeocoding;



    @Inject
    SearchPlacesPresenter(SavedAddressUseCase savedAddressUseCase,
                          GetSavedAddressUseCase getSavedAddressUseCase,
                          GetPlacesUseCase getPlacesUseCase,
                          GetPlaceBufferUseCase getPlaceBufferUseCase)
    {
        this.savedAddressUseCase = savedAddressUseCase;
        this.getSavedAddressUseCase = getSavedAddressUseCase;
        this.getPlacesUseCase = getPlacesUseCase;
        this.getPlaceBufferUseCase = getPlaceBufferUseCase;
    }


    @Override
    protected void updateViewState() {
        getSavedAddresses();
    }

    public void getSavedAddresses(){
        subscriptions.add(getSavedAddressUseCase
                .execute(new RxObserver<List<SavedAddress>>(view){
                    @Override
                    public void onNext(List<SavedAddress> savedAddresses) {
                        super.onNext(savedAddresses);
                        view.onSavedAddressSuccess(savedAddresses);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSavedAddressFailure();
                    }
                }));
    }

    public void saveSavedAddress(List<SavedAddress> savedAddresses){
        subscriptions.add(savedAddressUseCase
                .withSavedAddress(savedAddresses)
                .execute(new RxObserver<Boolean>(view){
                    @Override
                    public void onNext(Boolean status) {
                        super.onNext(status);
                        view.onSaveSavedAddressSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSaveSavedAddressFailure();
                    }
                }));
    }

    public void startGoogleSearching(String constraint){
        getPlaces(constraint);
    }

    public void getPlaces(String constraint){
        subscriptions.add(getPlacesUseCase
                .withConstraint(constraint)
                .execute(new RxObserver<ArrayList<PlaceAutocomplete>>(view){
                    @Override
                    public void onNext(ArrayList<PlaceAutocomplete> places) {
                        super.onNext(places);
                        view.onSearchResultSuccess(places);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.onSearchResultFailure();
                    }
                }));
    }


    public void getPlace(String placeId){
        subscriptions.add(getPlaceBufferUseCase
                .withPlaceId(placeId)
                .execute(new RxObserver<Place>(view){
                    @Override
                    public void onNext(Place placeBuffer) {
                        super.onNext(placeBuffer);
                        if(view!=null){
                            view.onPlaceSuccess(placeBuffer);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(view!=null){
                            view.onPlaceFailure();
                        }
                    }
                }));
    }


    public void startMapboxSearching(String access_token, String s){
        mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(access_token)
                .query(s)
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();
                if (results.size() > 0) {
                    ArrayList resultList = new ArrayList<>(results.size());
                    for(CarmenFeature carmenFeature:results) {
                        PlaceAutocomplete placeAutocomplete = new PlaceAutocomplete();
                        placeAutocomplete.setLatitude(((Point) carmenFeature.geometry()).latitude());
                        placeAutocomplete.setLongitude(((Point) carmenFeature.geometry()).longitude());
                        placeAutocomplete.setAddress1(carmenFeature.placeName());
                        placeAutocomplete.setPlaceId(carmenFeature.id());
                        placeAutocomplete.setCarmenFeature(carmenFeature);
                        resultList.add(placeAutocomplete);
                    }
                    if(view!=null){
                        view.onSearchResultSuccess(resultList);
                    }
                }else{
                    if(view!=null){
                        view.onSearchResultFailure();
                    }
                }
            }
            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                if(view!=null){
                    view.onSearchResultFailure();
                }
            }
        });
    }

    public void cancelMapboxSearching(){
        if(mapboxGeocoding!=null){
            mapboxGeocoding.cancelCall();
        }
    }

}
