package com.lattis.ellipse.presentation.ui.damagebike;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.ui.utils.ImageCompressor;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity;
import com.lattis.ellipse.presentation.ui.utils.PopUpActivity3;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.model.Media;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.lattis.ellipse.presentation.ui.damagebike.DamageBikeActivityPermissionsDispatcher.requestLocationUpdatesWithPermissionCheck;
import static com.lattis.ellipse.presentation.ui.damagebike.DamageBikeActivityPermissionsDispatcher.takePictureWithPermissionCheck;
import static com.lattis.ellipse.presentation.ui.ride.EndRideFragment.DAMAGE_REPORT_SUCCESS;

@RuntimePermissions
public class DamageBikeActivity extends BaseCloseActivity<DamageBikePresenter> implements DamageBikeView, AdapterView.OnItemSelectedListener {
    public static final String DAMAGE_REPORT_CANCEL_BOOKING = "DAMAGE_REPORT_CANCEL_BOOKING";
    private static final int REQUEST_CODE_FOR_IMAGE = 1000;
    private static final int REQUEST_CODE_FOR_DESCRIPTION = 2000;
    private int POP_UP_REQUEST_CODE = 4531;
    private int REQUEST_CODE_FOR_FAILURE = 4532;
    private int REQUEST_CODE_FOR_BOOKING_CONTINUE_OR_CANCEL=4533;
    private String description;
    @BindView(R.id.tv_damage_desc)
    TextView descriptionTextView;
    @BindView(R.id.tv_photo_path)
    TextView imagePathTextView;
    private final String DESCRIPTION_TAG = "DESCRIPTION";
    private String uploadUrl = null;
    private int bikeId=0;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView((R.id.rl_loading_operation))
    View damage_bike_loading_operation_view;
    @BindView(R.id.label_operation_name)
    CustomTextView Damage_bike_loading_operation_name;


    @BindView(R.id.submit_report_enabled_btn)
    CustomButton submit_report_enabled_btn;

    @BindView(R.id.submit_report_disabled_btn)
    CustomButton submit_report_disabled_btn;



    boolean isRideStarted;
    private int tripID;
    private static final int REQUEST_END_RIDE_CHECKLIST = 12013;


    @Inject
    DamageBikePresenter damageBikePresenter;
    private String[] categoryName;
    private String[] category;



    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected DamageBikePresenter getPresenter() {
        return damageBikePresenter;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.title_submit_damage_report));
        spinner.setOnItemSelectedListener(this);
        updateSpinner();

    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_damage_bike;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLocationUpdatesWithPermissionCheck(this);
        category = getResources().getStringArray(R.array.damage_category);
        getPresenter().setCategoryList(category);
    }

    @OnClick(R.id.tv_damage_desc)
    public void onClickDescription(View v) {
        description = descriptionTextView.getText().toString();
        startActivityForResult(new Intent(this, DescriptionActivity.class).putExtra(DESCRIPTION_TAG, description), REQUEST_CODE_FOR_DESCRIPTION);
    }

    @OnClick(R.id.submit_report_enabled_btn)
    public void onClickReportSend() {
        if(bikeId==0){
            getPresenter().getCurrentUserStatus();
            //getPresenter().getRide();
        }else if (description != null && !description.isEmpty() && uploadUrl!=null) {
            damageBikePresenter.updateDamageReport(bikeId, description, uploadUrl,tripID);
        }else{
            submit_report_disabled_btn.setVisibility(View.VISIBLE);
            submit_report_enabled_btn.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.tv_photo_path)
    public void onClickCamera(View v) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            takePictureWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            takePictureWithPermissionCheck(this);
        }else{
            takePicture();
        }

    }


    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void takePicture() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        SandriosCamera
                .with()
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .enableImageCropping(false)
                .launchCamera(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    public void requestLocationUpdates() {
        damage_bike_loading_operation_view.setVisibility(View.GONE);
        damageBikePresenter.getCurrentUserStatus();
        //getPresenter().getRide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_DESCRIPTION && resultCode == RESULT_OK && data != null) {
            description = data.getStringExtra(DESCRIPTION_TAG);
            descriptionTextView.setText(description);
            setSubmitButtonStatus();
        } else if (resultCode == Activity.RESULT_OK
                && requestCode == SandriosCamera.RESULT_CODE
                && data != null) {
            if (data.getSerializableExtra(SandriosCamera.MEDIA) instanceof Media) {
                Media media = (Media) data.getSerializableExtra(SandriosCamera.MEDIA);
                Log.e("File", "" + media.getPath());
                Log.e("Type", "" + media.getType());
                String filePath = media.getPath();
                filePath = ImageCompressor.resizeAndCompressImageBeforeSend(this, filePath, "damage");
                if(filePath!=null) {
                    damageBikePresenter.uploadImage(filePath);
                    showLoading(getString(R.string.uploading));
                }
            }
        }else if (requestCode == POP_UP_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
            return;
        } else if (requestCode == REQUEST_END_RIDE_CHECKLIST) {
            if(resultCode == RESULT_OK){
                if(data!=null){
                    if(data.hasExtra(DAMAGE_REPORT_SUCCESS)){
                        if(data.getExtras().getBoolean(DAMAGE_REPORT_SUCCESS)){
                            Intent intent = new Intent();
                            intent.putExtra(DAMAGE_REPORT_SUCCESS,true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                }
            }else{
                setResult(RESULT_OK);
                finish();
            }

        } else if (requestCode == REQUEST_CODE_FOR_BOOKING_CONTINUE_OR_CANCEL){
            if(resultCode == RESULT_OK){
                finish();
            }else{
                Intent intent = new Intent();
                intent.putExtra(DAMAGE_REPORT_CANCEL_BOOKING, true);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DamageBikeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void uploadImageSuccess(String imageUrl) {
        this.uploadUrl = imageUrl;
        imagePathTextView.setText(imageUrl);
        setSubmitButtonStatus();
    }

    @Override
    public void uploadImageFail() {
        uploadUrl=null;
        PopUpActivity.launchForResult(this, REQUEST_CODE_FOR_FAILURE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), "", getString(R.string.ok));
    }

    @Override
    public void updateSpinner() {
        categoryName = getResources().getStringArray(R.array.damage_category_name);
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryName);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);
    }

    @Override
    public void selectCategory() {
        Toast.makeText(this, "Please select the category", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    @Override
    public void setTripID(int tripID) {
        this.tripID = tripID;
        damageBikePresenter.getRideSummary(tripID);
    }

    @Override
    public void showLoading(String message) {
        damage_bike_loading_operation_view.setVisibility(View.VISIBLE);
        Damage_bike_loading_operation_name.setText(message);
    }

    @Override
    public void hideProgressLoading() {
        damage_bike_loading_operation_view.setVisibility(View.GONE);

    }

    @Override
    public void showBikeNotYetBooked() {
        finish();
    }

    @Override
    public void damageReportSuccess() {
        if (isRideStarted) {
            DamageReportSuccessActivity.launchActivity(this, tripID, REQUEST_END_RIDE_CHECKLIST);
        } else {
            PopUpActivity3.launchForResult(this,
                    REQUEST_CODE_FOR_BOOKING_CONTINUE_OR_CANCEL,
                    getString(R.string.thanks),
                    getString(R.string.damage_report_success_description),
                    null,
                    getString(R.string.btn_continue),
                    getString(R.string.damage_report_success_cancel_booking)
                    );
        }
    }

    @Override
    public void damageReportFailure() {
        PopUpActivity.launchForResult(this, REQUEST_CODE_FOR_FAILURE, getString(R.string.alert_error_server_title),
                getString(R.string.alert_error_server_subtitle), null
                , getString(R.string.ok));

    }

    @Override
    public void isRideStarted(boolean started) {
        this.isRideStarted = started;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        damageBikePresenter.setPosition(position);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

    private void setSubmitButtonStatus(){
        if (description != null && !description.isEmpty() && uploadUrl!=null) {
            submit_report_disabled_btn.setVisibility(View.GONE);
            submit_report_enabled_btn.setVisibility(View.VISIBLE);
        }else{
            submit_report_disabled_btn.setVisibility(View.VISIBLE);
            submit_report_enabled_btn.setVisibility(View.GONE);
        }
    }

}
