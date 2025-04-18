package com.lattis.ellipse.presentation.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import  androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lattis.ellipse.domain.model.PrivateNetwork;
import com.lattis.ellipse.domain.model.User;
import com.lattis.ellipse.presentation.ui.base.activity.BaseBackArrowActivity;
import com.lattis.ellipse.presentation.ui.profile.addcontact.AddMobileNumberActivity;
import com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailActivity;
import com.lattis.ellipse.presentation.ui.profile.delete.DeleteAccountActivity;
import com.lattis.ellipse.presentation.ui.profile.updatePassword.UpdatePasswordActivity;
import com.lattis.ellipse.presentation.view.BannerView;
import com.lattis.ellipse.presentation.view.CustomEditText;
import com.lattis.ellipse.presentation.view.CustomTextView;
import com.lattis.ellipse.presentation.view.utils.BlurTransformation;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.lattis.ellipse.presentation.ui.profile.ProfileActivityPermissionsDispatcher.pickProfileImageWithPermissionCheck;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_ACCOUNT_TYPE;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_FLEET_PRESENT;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.ARG_USER_ID;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.USER_ACCOUNT_TYPE_MAIN;
import static com.lattis.ellipse.presentation.ui.profile.changeMail.ChangeMailPresenter.USER_ACCOUNT_TYPE_PRIVATE;


@RuntimePermissions
public class ProfileActivity extends BaseBackArrowActivity<ProfilePresenter> implements ProfileView {
    private final int REQUEST_CODE_DELETE_ACCOUNT = 5100;


    @BindView(R.id.iv_wallpaper_blur)
    BannerView imageView_wallpaper_blur;
    @BindView(R.id.iv_wallpaper)
    ImageView imageView_wallpaper;

    @BindView(R.id.iv_getimage)
    ImageView getImageView;

    @BindView((R.id.rl_loading_operation))
    View profile_loading_operation_view;
    @BindView(R.id.label_operation_name)
    CustomTextView loading_operation_name;

    private final static int PERMISSION_CODE_MEDIA = 2909;
    private final int REQUEST_CODE_ADD_PRIVATE_NETWORK = 2092;
    @BindView(R.id.et_first_name)
    EditText firstNameInput;
    @BindView(R.id.et_last_name)
    EditText lastNameInput;
    @BindView(R.id.et_email)
    EditText emailInput;
    @BindView(R.id.et_phone_number)
    EditText phoneNumberInput;

    @BindView(R.id.tv_private_network_content)
    CustomTextView tv_no_private_network_content;


    @BindView(R.id.rv_private_network)
    RecyclerView rv_private_network;

    @BindView(R.id.tv_add_private_network)
    TextView tv_add_private_network;

    @BindView(R.id.view9)
    View line_View;


    @Inject
    ProfilePresenter profilePresenter;
    private String userId;
    private boolean userFleetPresent;
    PrivateNetworkAdapter privateNetworkAdapter;


    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected ProfilePresenter getPresenter() {
        return profilePresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.profile_settings));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.next_add_number)
    public void onAddMobileNumberviewClicked() {
        startActivity(new Intent(this, AddMobileNumberActivity.class));
    }

    @OnClick(R.id.next_change_password)
    public void onUpdatePasswordviewClicked() {
        startActivity(new Intent(this, UpdatePasswordActivity.class));
    }

    @OnClick(R.id.next_deleteaccount)
    public void onDeleteAccountClicked() {
        startActivityForResult(new Intent(this, DeleteAccountActivity.class),REQUEST_CODE_DELETE_ACCOUNT);
    }

    @OnClick(R.id.next_change_mail)
    public void onChangeEmailButtonClicked() {
        startActivity(new Intent(this, ChangeMailActivity.class)
                .putExtra(ARG_USER_ACCOUNT_TYPE, USER_ACCOUNT_TYPE_MAIN)
                .putExtra(ARG_USER_ID, userId));
    }

    @OnClick(R.id.tv_add_private_network)
    public void onAddPrivateNetworkclicked() {
        startActivityForResult(new Intent(this, ChangeMailActivity.class)
                .putExtra(ARG_USER_ACCOUNT_TYPE, USER_ACCOUNT_TYPE_PRIVATE)
                .putExtra(ARG_USER_FLEET_PRESENT,userFleetPresent)
                .putExtra(ARG_USER_ID, userId),REQUEST_CODE_ADD_PRIVATE_NETWORK);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasyImage.configuration(this)
                .setImagesFolderName("Lattis")
                .setAllowMultiplePickInGallery(false);

        fetchUserProfile();
    }

    private void fetchUserProfile(){
        showLoading(getString(R.string.loading));
        getPresenter().getUserProfile();
    }

    private void showPrivateNetworks(List<PrivateNetwork> privateNetworks){
        if(rv_private_network.getAdapter()==null || privateNetworkAdapter == null) {
            privateNetworkAdapter = new PrivateNetworkAdapter(this);
            privateNetworkAdapter.setPrivateNetworks(privateNetworks);
            rv_private_network.setAdapter(privateNetworkAdapter);
        }else{
            privateNetworkAdapter.setPrivateNetworks(privateNetworks);
            privateNetworkAdapter.notifyDataSetChanged();
        }
    }


    @OnClick(R.id.iv_wallpaper)
    public void getImage(final View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pickProfileImageWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pickProfileImageWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pickProfileImageWithPermissionCheck(this);
        }else{
            pickProfileImage();
        }

    }


    @OnClick(R.id.iv_getimage)
    public void onGetImageViewClicked(final View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pickProfileImageWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pickProfileImageWithPermissionCheck(this);
        }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pickProfileImageWithPermissionCheck(this);
        }else{
            pickProfileImage();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ProfileActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if(!getPresenter().readyToSave()){
                return true;
            }
            hideKeyboard();
            showLoading(getString(R.string.loading));
            getPresenter().updateUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void pickProfileImage() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        EasyImage.openChooserWithGallery(this, "", 0);
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        subscriptions.add(RxTextView.textChangeEvents(firstNameInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setFirstName(textViewTextChangeEvent.text().toString())));
        subscriptions.add(RxTextView.textChangeEvents(lastNameInput)
                .subscribe(textViewTextChangeEvent -> getPresenter().setLastName(textViewTextChangeEvent.text().toString())));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_CODE_DELETE_ACCOUNT && resultCode == RESULT_OK){
            finish();
        }else if(requestCode == REQUEST_CODE_ADD_PRIVATE_NETWORK && resultCode == RESULT_OK) {
            fetchUserProfile();
        }else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    //Some error handling
                }

                @Override
                public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {
                    //Handle the images
                    if(imagesFiles!=null && imagesFiles.size()==1){
                        handleCrop(Uri.fromFile(imagesFiles.get(0)));
                    }
                }
            });
        }
    }

    public void showLoading(String message) {
        profile_loading_operation_view.setVisibility(View.VISIBLE);
        loading_operation_name.setText(message);
    }

    public void hideProgressLoading() {
        profile_loading_operation_view.setVisibility(View.GONE);
    }


    public void handleCrop(Uri sourceUri) {
        getPresenter().setImageUri(sourceUri.toString());
        getImageView.setVisibility(View.VISIBLE);
        imageView_wallpaper.setEnabled(false);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.circleCrop();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);


        Glide.with(this)
                .load(sourceUri)
                .apply(requestOptions)
                .into(imageView_wallpaper);


        RequestOptions requestOptionsBlur = new RequestOptions();
        requestOptionsBlur.transform(new BlurTransformation(this));
        requestOptionsBlur.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(this)
                .load(sourceUri)
                .apply(requestOptionsBlur)
                .into(imageView_wallpaper_blur);
    }


    @Override
    public void setLastName(String lastName) {
        lastNameInput.setText(lastName);
        checkForHintAndError("lastname",lastName);
    }

    @Override
    public void setFirstName(String firstName) {
        hideProgressLoading();
        firstNameInput.setText(firstName);
        checkForHintAndError("firstname",firstName);
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        phoneNumberInput.setText(phoneNumber);
        checkForHintAndError("phonenumber",phoneNumber);
    }

    @Override
    public void setEmail(String email) {
        emailInput.setText(email);
        checkForHintAndError("email",email);
    }

    @Override
    public void checkForHintAndError(String type,String value){
        switch(type){
            case "email":
                if(TextUtils.isEmpty(value))
                    emailInput.setHint(getString(R.string.email));
                else
                    emailInput.setHint("");
                break;
            case "firstname":
//                if(TextUtils.isEmpty(value))
//                    firstNameInput.setHint(getString(R.string.first_name));
//                else
//                    firstNameInput.setHint("");

                if(TextUtils.isEmpty(value)){
                    firstNameInput.setError("");
                }else{
                    firstNameInput.setError(null);
                }

                break;
            case "lastname":
//                if(TextUtils.isEmpty(value))
//                    lastNameInput.setHint(getString(R.string.last_name));
//                else
//                    lastNameInput.setHint("");

                if(TextUtils.isEmpty(value)){
                    lastNameInput.setError("");
                }else{
                    lastNameInput.setError(null);
                }
                break;
            case "phonenumber":
                if(TextUtils.isEmpty(value))
                    phoneNumberInput.setHint(getString(R.string.add_phone_number));
                else
                    phoneNumberInput.setHint("");
                break;
        }
    }



    @Override
    public void setImage(String imageUri) {
        getImageView.setVisibility(View.VISIBLE);
        imageView_wallpaper.setEnabled(false);
        handleCrop(Uri.parse(imageUri));
    }

    @Override
    public void setNoImage() {
        getImageView.setVisibility(View.GONE);
        imageView_wallpaper.setEnabled(true);
    }

    @Override
    public void setPrivateNetwork(List<PrivateNetwork> privateNetworks) {
        for (PrivateNetwork privateNetwork : privateNetworks) {
            Log.e("ProfileActivity", "\n" + privateNetwork.toString());
        }

        userFleetPresent = true;

        rv_private_network.setLayoutManager(new LinearLayoutManager(this));
        tv_no_private_network_content.setVisibility(View.INVISIBLE);
        line_View.setVisibility(View.GONE);
        rv_private_network.setVisibility(View.VISIBLE);
        showPrivateNetworks(privateNetworks);
        tv_add_private_network.setText(getString(R.string.add_another_private_network));
    }

    @Override
    public void setNoPrivateNetwork() {
        userFleetPresent = false;
        tv_no_private_network_content.setVisibility(View.VISIBLE);
        rv_private_network.setVisibility(View.INVISIBLE);
        tv_add_private_network.setText(getString(R.string.add_private_network));
    }


    @Override
    public void showEmailError(@StringRes int error) {
        hideProgressLoading();
    }

    @Override
    public void hideEmailError() {
        hideProgressLoading();
    }

    @Override
    public void onUserUpdated(User user) {
        hideProgressLoading();
    }

    @Override
    public void onUserUpdateFailed() {
        hideProgressLoading();
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }


    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
