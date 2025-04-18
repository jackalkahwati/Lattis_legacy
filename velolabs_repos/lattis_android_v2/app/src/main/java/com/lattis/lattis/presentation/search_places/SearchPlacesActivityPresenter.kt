package com.lattis.lattis.presentation.search_places

import com.google.android.libraries.places.api.model.Place
import com.lattis.domain.models.Bike
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.domain.usecase.location.GetPlaceBufferUseCase
import com.lattis.domain.usecase.location.GetPlacesUseCase
import com.lattis.domain.usecase.saveaddress.GetSavedAddressUseCase
import com.lattis.domain.usecase.saveaddress.SavedAddressUseCase
import com.lattis.domain.models.Location
import com.lattis.domain.models.SavedAddress
import com.lattis.domain.usecase.bike.SearchBikeByNameUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import com.lattis.lattis.uimodel.mapper.SavedToAutoPlaceAddressMapper
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class SearchPlacesActivityPresenter @Inject constructor(
    private val getPlacesUseCase: GetPlacesUseCase,
    private val getPlaceBufferUseCase: GetPlaceBufferUseCase,
    private val savedAddressUseCase: SavedAddressUseCase,
    private val getSavedAddressUseCase: GetSavedAddressUseCase,
    private val savedToAutoPlaceAddressMapper: SavedToAutoPlaceAddressMapper,
    private val searchBikeByNameUseCase: SearchBikeByNameUseCase
) : ActivityPresenter<SearchPlacesActivityView>(){


    var clickedPlaceAutocomplete: PlaceAutocomplete?=null
    var clickedLocation:Location?=null
    var mSavedAddressList: ArrayList<SavedAddress>? = null
    var mPlaceAutocompletesFromSavedAddress : ArrayList<PlaceAutocomplete>? =null
    var bikes:List<Bike>?=null
    var selectedBike:Bike?=null
    var places: ArrayList<PlaceAutocomplete>?=null


    override fun updateViewState() {
        super.updateViewState()
        getSavedAddresses()
    }

    fun startGoogleSearching(constraint: String) {
        getPlaces(constraint!!)
    }

    fun getPlaces(constraint: String) {
        places?.clear()
        subscriptions.add(
            getPlacesUseCase
                .withConstraint(constraint!!)
                .execute(object : RxObserver<ArrayList<PlaceAutocomplete>>(view) {
                    override fun onNext(newPlaces: ArrayList<PlaceAutocomplete>) {
                        super.onNext(newPlaces)
                        places = newPlaces
                        searchBikeByName(constraint)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        searchBikeByName(constraint)
                    }
                })
        )
    }


    fun getPlace(placeAutocomplete: PlaceAutocomplete,placeId: String?) {
        this.clickedPlaceAutocomplete = placeAutocomplete
        subscriptions.add(
            getPlaceBufferUseCase
                .withPlaceId(placeId!!)
                .execute(object : RxObserver<Place>(view) {
                   override fun onNext(placeBuffer: Place) {
                        super.onNext(placeBuffer)
                       clickedLocation = Location(placeBuffer.getLatLng()?.latitude!!,placeBuffer.getLatLng()?.longitude!!)

                       val savedAddress = SavedAddress()
                       savedAddress.id= (placeBuffer.getId())
                       savedAddress.address1 = clickedPlaceAutocomplete?.address1!!.toString()
                       savedAddress.address2 = clickedPlaceAutocomplete?.address2!!.toString()
                       savedAddress.latitude = (placeBuffer.getLatLng()?.latitude)
                       savedAddress.longitude = (placeBuffer.getLatLng()?.longitude)
                       if (mSavedAddressList == null) mSavedAddressList = ArrayList<SavedAddress>()
                       mSavedAddressList?.add(savedAddress)
                       saveSavedAddress(mSavedAddressList!!)

                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                    }
                })
        )
    }

    fun saveSavedAddress(savedAddresses: List<SavedAddress>) {
        subscriptions.add(
            savedAddressUseCase
                .withSavedAddress(savedAddresses)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(status: Boolean) {
                        super.onNext(status)
                        view.onAddressedSavedSuccessfullyAfterClick()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                    }
                })
        )
    }


    fun getSavedAddresses() {
        subscriptions.add(
            getSavedAddressUseCase
                .execute(object : RxObserver<List<SavedAddress>>(view) {
                    override fun onNext(savedAddresses: List<SavedAddress>) {
                        super.onNext(savedAddresses)

                        if (mSavedAddressList == null) mSavedAddressList = ArrayList<SavedAddress>()
                        mSavedAddressList?.addAll(savedAddresses)

                        if(mSavedAddressList?.size!!>0) {
                            if (mPlaceAutocompletesFromSavedAddress == null) mPlaceAutocompletesFromSavedAddress = ArrayList<PlaceAutocomplete>()
                            mPlaceAutocompletesFromSavedAddress?.addAll(savedToAutoPlaceAddressMapper.mapIn(mSavedAddressList!!))
                            view?.onSuccessfullFetchOfSavedAddress()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e!!)
                    }
                })
        )
    }


    fun searchBikeByName(bike_name: String?) {
        subscriptions.add(
            searchBikeByNameUseCase
                .withBikeName(bike_name)
                .execute(object : RxObserver<List<Bike>>(view) {
                    override fun onNext(newBikes: List<Bike>) {
                        super.onNext(newBikes)
                        bikes = newBikes
                        if(places==null){
                            places = ArrayList()
                        }

                        for(bike in bikes!!){
                            if(bikeAlreadyInList(bike))continue
                            var placeAutocomplete = PlaceAutocomplete()
                            placeAutocomplete.bike = bike
                            places?.add(placeAutocomplete)
                        }

                        view?.onSearchResultSuccess(places!!)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if(places==null){
                            view?.onSearchResultFailure()
                        }else{
                            view?.onSearchResultSuccess(places!!)
                        }
                    }
                })
        )
    }

    fun bikeAlreadyInList(bike: Bike):Boolean{
        if(places != null && places?.size!!>0) {
            for (place in places!!) {
                if (place.bike != null && place.bike?.bike_id!!.equals(bike.bike_id)) return true
            }
        }
        return false
    }

    fun saveBikeAddress(bike: Bike){
        clickedLocation = Location(bike.latitude!!,bike.longitude!!)
        selectedBike = bike
        view?.onBikeSelectedDone()
    }


}