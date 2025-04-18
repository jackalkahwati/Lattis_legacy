package com.lattis.ellipse.presentation.ui.bike;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.libraries.places.api.model.Place;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.SavedAddress;
import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;
import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.view.CustomEditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;


public class SearchPlacesActivity extends BaseCloseActivity<SearchPlacesPresenter> implements SearchPlacesView,PlaceAutocompleteAdapter.PlaceAutoCompleteInterface, PlaceSavedAdapter.SavedPlaceListener {
    @Inject
    SearchPlacesPresenter searchPlacesPresenter;
    @BindView(R.id.rv_search_list)
    RecyclerView mRecyclerView;
    PlaceAutocompleteAdapter mAdapter;
    @BindView(R.id.et_search)
    CustomEditText searchView;
    @BindView(R.id.iv_search_cancel)
    ImageView iv_SearchCancel;

    @BindView(R.id.pb_search)
    ProgressBar progressBar;

    private List<SavedAddress> mSavedAddressList;
    private PlaceSavedAdapter mSavedAdapter;
    private Location clickedLocation;

    public static final String SEARCH_LOCATION_LONGITUDE = "SEARCH_LOCATION_LONGITUDE";
    public static final String SEARCH_LOCATION_LATITUDE = "SEARCH_LOCATION_LATITUDE";
    public static final String SEARCH_CURRENT_LOCATION = "SEARCH_CURRENT_LOCATION";



    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected SearchPlacesPresenter getPresenter() {
        return searchPlacesPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_search_places;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.pick_up_location));
    }

    @OnClick(R.id.iv_search_cancel)
    public void cancelClicked() {
        searchView.setText("");
        if (mAdapter != null) {
            mAdapter.clearList();
        }
    }

    @OnClick(R.id.ct_current_location)
    public void setLocationToCurrentLocation(){
        Intent data = new Intent();
        data.putExtra(SEARCH_CURRENT_LOCATION, true);
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new PlaceAutocompleteAdapter(this, R.layout.row_search_places);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    if (mAdapter != null) {
                        mRecyclerView.setAdapter(mAdapter);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        iv_SearchCancel.setImageResource(R.drawable.ic_close);
                    }
                } else {

                    if (mSavedAdapter != null && mSavedAddressList.size() > 0) {
                        mRecyclerView.setAdapter(mSavedAdapter);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }else{
                        mRecyclerView.setVisibility(View.GONE);
                    }


                    iv_SearchCancel.setImageResource(R.drawable.ic_qr_code);

                }
                if (!s.toString().equals("")) {
                    progressBar.setVisibility(View.VISIBLE);
                    getPresenter().startGoogleSearching(s.toString());  // use this for Google API search
//                    getPresenter().startMapboxSearching(getString(R.string.map_box_access_token),s.toString());    // use this for Mapbox API search
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocomplete> mResultList, int position) {
        if (mResultList != null) {
            try {

                // Mapbox API search
//                clickedLocation = new Location(mResultList.get(position).getLatitude(), mResultList.get(position).getLongitude());
//                SavedAddress savedAddress = new SavedAddress();
//                savedAddress.setId(String.valueOf(mResultList.get(position).getPlaceId()));
//                savedAddress.setName(String.valueOf(mResultList.get(position).getAddress1()));
//                savedAddress.setLatitude(mResultList.get(position).getLatitude());
//                savedAddress.setLongitude(mResultList.get(position).getLongitude());
//                if(mSavedAddressList==null) mSavedAddressList = new ArrayList<>();
//                mSavedAddressList.add(savedAddress);
//                getPresenter().saveSavedAddress(mSavedAddressList);

                    //Google API search
                final String placeId = String.valueOf(mResultList.get(position).getPlaceId());
                getPresenter().getPlace(placeId);
            } catch (Exception e) {
                Log.e("Exception",e.getLocalizedMessage());
            }

        }

    }





    public static void launchForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, SearchPlacesActivity.class), requestCode);
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onSavedPlaceClick(SavedAddress savedAddress) {
        if(savedAddress!=null){
            try {
                Intent data = new Intent();
                data.putExtra(SEARCH_LOCATION_LATITUDE,savedAddress.getLatitude());
                data.putExtra(SEARCH_LOCATION_LONGITUDE, savedAddress.getLongitude());
                setResult(SearchPlacesActivity.RESULT_OK, data);
                finish();
            }
            catch (Exception e){

            }

        }
    }


    @Override
    public void onSavedAddressSuccess(List<SavedAddress> savedAddresses) {
        this.mSavedAddressList = savedAddresses;
        if(mSavedAddressList==null) mSavedAddressList = new ArrayList<>();
        if(mSavedAddressList.size()>0){
            mSavedAdapter = new PlaceSavedAdapter(this,mSavedAddressList);
            mRecyclerView.setAdapter(mSavedAdapter);
        }
    }

    @Override
    public void onSavedAddressFailure() {

    }

    @Override
    public void onSaveSavedAddressSuccess() {
        returnClickedAddress();
    }

    @Override
    public void onSaveSavedAddressFailure() {
        returnClickedAddress();
    }

    private void returnClickedAddress(){
        Intent data = new Intent();
        if(clickedLocation!=null) {
            data.putExtra(SEARCH_LOCATION_LATITUDE, clickedLocation.getLatitude());
            data.putExtra(SEARCH_LOCATION_LONGITUDE, clickedLocation.getLongitude());
        }else{
            data.putExtra(SEARCH_CURRENT_LOCATION, true);
        }
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    public void onSearchResultSuccess(ArrayList<PlaceAutocomplete> results) {
        progressBar.setVisibility(View.GONE);
        mAdapter.setSearchResult(results);
    }

    @Override
    public void onSearchResultFailure() {
        progressBar.setVisibility(View.GONE);
//        mAdapter.setSearchResult(new ArrayList<>());
    }


    @Override
    public void onPlaceSuccess(Place place) {
            clickedLocation = new Location(place.getLatLng().latitude,place.getLatLng().longitude);
            SavedAddress savedAddress = new SavedAddress();
            savedAddress.setId(place.getId());
            savedAddress.setName(place.getAddress().toString());
            savedAddress.setLatitude(place.getLatLng().latitude);
            savedAddress.setLongitude(place.getLatLng().longitude);
            if(mSavedAddressList==null) mSavedAddressList = new ArrayList<>();
            mSavedAddressList.add(savedAddress);
            getPresenter().saveSavedAddress(mSavedAddressList);
    }

    @Override
    public void onPlaceFailure() {
        returnClickedAddress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().cancelMapboxSearching();
    }
}
