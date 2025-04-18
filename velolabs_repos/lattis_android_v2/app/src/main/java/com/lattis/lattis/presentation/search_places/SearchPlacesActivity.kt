package com.lattis.lattis.presentation.search_places


import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.domain.models.map.PlaceAutocomplete
import com.lattis.domain.models.Location
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import com.lattis.lattis.presentation.utils.FirebaseUtil
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.activity_search_places_content.*
import java.util.*
import javax.inject.Inject


class SearchPlacesActivity : BaseActivity<SearchPlacesActivityPresenter, SearchPlacesActivityView>(),
    SearchPlacesActivityView, PlaceAutocompleteAdapter.PlaceAutoCompleteInterface {

    companion object{
        const val SEARCH_LOCATION_LONGITUDE = "SEARCH_LOCATION_LONGITUDE"
        const val SEARCH_LOCATION_LATITUDE = "SEARCH_LOCATION_LATITUDE"
        const val SEARCH_CURRENT_LOCATION = "SEARCH_CURRENT_LOCATION"
        const val SEARCH_BIKE_ID = "SEARCH_BIKE_ID"
    }

    private var placeAutocompleteAdapter:PlaceAutocompleteAdapter?=null

    @Inject
    override lateinit var presenter: SearchPlacesActivityPresenter
    override val activityLayoutId = R.layout.activity_search_places
    override var view: SearchPlacesActivityView = this

    override fun configureViews() {
        super.configureViews()
        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this)
        rv_places_in_search_places.setAdapter(placeAutocompleteAdapter)
        rv_places_in_search_places.setHasFixedSize(true)
        rv_places_in_search_places.setLayoutManager(LinearLayoutManager(this))


        et_search_address_in_search_places.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (count > 0) {
                    if (placeAutocompleteAdapter != null) {
                        rv_places_in_search_places.setAdapter(placeAutocompleteAdapter)
                    }
                } else {
                    if(presenter.mPlaceAutocompletesFromSavedAddress!=null){
                        placeAutocompleteAdapter?.setSearchResult(presenter.mPlaceAutocompletesFromSavedAddress)
                    }
                }
                if (s.toString() != "") {
                    presenter.startGoogleSearching(s.toString()) // use this for Google API search
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        ct_cancel_search_in_search_places.setOnClickListener {
            finish()
        }
        FirebaseUtil.instance?.addCustomEvent(FirebaseUtil.SEARCH, FirebaseUtil.SEARCH)
    }

    override fun onSearchResultSuccess(results: ArrayList<PlaceAutocomplete>) {
        placeAutocompleteAdapter?.setSearchResult(results)
    }

    override fun onSearchResultFailure() {
    }

    override fun onPlaceClick(
        mResultList: ArrayList<PlaceAutocomplete>?,
        position: Int
    ) {
        if(mResultList!=null){
            if(mResultList[position].bike!=null) doNeedfulForBikeClick(mResultList,position) else doNeedfulForAddressClick(mResultList,position)
        }
    }

    fun doNeedfulForBikeClick(mResultList: ArrayList<PlaceAutocomplete>,
                                 position: Int){

        presenter.saveBikeAddress(mResultList[position].bike!!)

    }

    fun doNeedfulForAddressClick(mResultList: ArrayList<PlaceAutocomplete>,
                                 position: Int){
        try {
            val placeAutocomplete = mResultList[position]
            if (placeAutocomplete.latitude != null &&
                placeAutocomplete.longitude != null
            ) {
                presenter.clickedLocation =
                    Location(placeAutocomplete?.latitude!!, placeAutocomplete?.longitude!!)
                returnClickedAddress()
            } else {
                val placeId: String =
                    java.lang.String.valueOf(mResultList[position].placeId)
                presenter.getPlace(mResultList[position], placeId)
            }
        } catch (e: Exception) {

        }

    }

    override fun onAddressedSavedSuccessfullyAfterClick() {
        returnClickedAddress()
    }

    override fun onBikeSelectedDone() {
        returnClickedAddress()
    }

    private fun returnClickedAddress() {
        val data = Intent()
        if (presenter.clickedLocation != null) {
            data.putExtra(
                SearchPlacesActivity.SEARCH_LOCATION_LATITUDE,
                presenter.clickedLocation?.latitude
            )
            data.putExtra(
                SearchPlacesActivity.SEARCH_LOCATION_LONGITUDE,
                presenter.clickedLocation?.longitude
            )
        }

        if(presenter.selectedBike!=null){
            data.putExtra(
                SearchPlacesActivity.SEARCH_BIKE_ID,
                presenter.selectedBike?.bike_id
            )
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onSuccessfullFetchOfSavedAddress() {
        placeAutocompleteAdapter?.setSearchResult(presenter.mPlaceAutocompletesFromSavedAddress)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {

    }
}