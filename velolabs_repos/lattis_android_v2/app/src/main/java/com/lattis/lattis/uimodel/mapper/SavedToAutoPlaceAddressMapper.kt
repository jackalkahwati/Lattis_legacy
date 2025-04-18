package com.lattis.lattis.uimodel.mapper

import com.lattis.domain.mapper.base.AbstractDataMapper
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.domain.models.SavedAddress
import javax.inject.Inject

class SavedToAutoPlaceAddressMapper @Inject constructor() :
    AbstractDataMapper<SavedAddress, PlaceAutocomplete>() {
    override fun mapIn(savedAddress: SavedAddress?): PlaceAutocomplete {
        var placeAutocomplete = PlaceAutocomplete()
        if(savedAddress!=null){
            placeAutocomplete.address1 = savedAddress.address1
            placeAutocomplete.address2 = savedAddress.address2
            placeAutocomplete.latitude = savedAddress.latitude
            placeAutocomplete.longitude = savedAddress.longitude
        }
        return placeAutocomplete
    }

    override fun mapOut(out: PlaceAutocomplete?): SavedAddress? {
        return null
    }
}