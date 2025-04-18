package com.lattis.lattis.presentation.search_places

import com.google.android.libraries.places.api.model.Place
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.lattis.presentation.base.BaseView
import java.util.*

interface SearchPlacesActivityView :BaseView{


    fun onSearchResultSuccess(results: ArrayList<PlaceAutocomplete>)
    fun onSearchResultFailure()
    fun onAddressedSavedSuccessfullyAfterClick()

    fun onSuccessfullFetchOfSavedAddress()

    fun onBikeSelectedDone()

}