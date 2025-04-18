package com.lattis.ellipse.presentation.ui.parking;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.target.SimpleTarget;
//import com.bumptech.glide.request.transition.Transition;

/**
 * Created by ssd3 on 3/28/17.
 */

public class ParkingDetailFragment extends BaseFragment<ParkingDetailFragmentPresenter> implements ParkingDetailFragmentView {

    private final String TAG = ParkingDetailFragment.class.getName();
    private int REQUEST_CODE_FOR_GOOGLE_MAP_APP = 283;

    @Inject
    ParkingDetailFragmentPresenter presenter;

    @BindView(R.id.tv_parking_name)
    TextView parkingName;
    @BindView(R.id.tv_parking_detail)
    TextView parkingDetail;
    @BindView(R.id.iv_parking_img)
    ImageView parkingImage;

    private Parking parking;
    private FindParkingFragment findParkingFragment;

    @OnClick(R.id.iv_parking_layout_close)
    public void close(){
        findParkingFragment.hideParkingDetailFragment();
    }

    @OnClick(R.id.tv_get_parking_route)
    public void getDirectionForParking(){
//        findParkingFragment.getDirectionForParking(parking);
        openGoogleMapApp(parking);
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @NonNull
    @Override
    protected ParkingDetailFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.parking_detail_fragment;
    }

    public void setParentFragment(FindParkingFragment findParkingFragment){
            this.findParkingFragment=findParkingFragment;
    }

    public void setParking(Parking parking) {
        this.parking = parking;
        refreshViews();
    }

    public void refreshViews() {
        if (parking != null) {
            parkingName.setText(parking.getName());
            parkingDetail.setText(parking.getDescription());
            downloadImage();
        }
    }




    void downloadImage(){

// TODO changed for Mapbox
        Glide
                .with(getActivity())
                .asBitmap()
                .load(parking.getPic())
                .into(new SimpleTarget<Bitmap>(100,100) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        parkingImage.setImageBitmap(resource); // Possibly runOnUiThread()
                        parkingDetail.setText(parking.getDescription());
                    }
                });
    }

    private void openGoogleMapApp(Parking parking){

        try {
            String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f(%s)&mode=bicycling", parking.getLatitude(), parking.getLongitude(), parking.getName());
            Uri gmmIntentUri = Uri.parse(uri);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) == null) {
                // Google map not installed
            } else {
                startActivityForResult(mapIntent, REQUEST_CODE_FOR_GOOGLE_MAP_APP);
            }
        }catch (ActivityNotFoundException e){

        }
    }

}
