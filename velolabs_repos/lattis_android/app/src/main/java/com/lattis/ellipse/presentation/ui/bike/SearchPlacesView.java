package com.lattis.ellipse.presentation.ui.bike;

import com.google.android.libraries.places.api.model.Place;
import com.lattis.ellipse.domain.model.SavedAddress;
import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lattis on 28/08/17.
 */

public interface SearchPlacesView extends BaseView {

    void onSavedAddressSuccess(List<SavedAddress> savedAddresses);
    void onSavedAddressFailure();


    void onSaveSavedAddressSuccess();
    void onSaveSavedAddressFailure();

    void onSearchResultSuccess(ArrayList<PlaceAutocomplete> results);
    void onSearchResultFailure();

    void onPlaceSuccess(Place place);
    void onPlaceFailure();
}
