package cc.skylock.skylock.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import cc.skylock.skylock.Bean.UpdateUserDetails;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.AccountSettingsMenuApater;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.ui.UiUtils.BannerView;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Velo Labs Android on 01-08-2016.
 */
public class UserProfileFragment extends Fragment implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener {
    private  Context mContext;
    private PrefUtil mPrefUtil;
    public static UserProfileFragment userProfileFragment = null;
    private  TextView textView_label_profiledetails, textView_label_firstName, textView_label_lastName,
            textView_label_phoneNumber, textView_label_email, textView_accountSettings;
    private View view;
    private EditText editText_firstName, editText_lastName, editText_phoneNumber, editText_email;
    private RecyclerView mRecyclerView;
    private BannerView imageView_wallpaper_blur;
    private ImageView imageView_wallpaper, imageView_getImage;
    private RelativeLayout relativeLayout_wallpaperForeground, relativeLayoutFisrtname,
            relativeLayoutLastName, relativeLayoutPhoneNumber, relativeLayoutEmail;
    private String title[] = {"Change my number",
            "Change my password", "Delete my account", "Log out"};
    private boolean isLogout = true;
    private int PICK_IMAGE_REQUEST = 1;
    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;
    boolean isFieldChange = false;
    private static UserRegistrationResponse mUserRegistrationResponse;
    private boolean isFacebooklogin = false;
    private final int PERMISSION_CODE_MEDIA = 2909;

    public static UserProfileFragment newInstance() {
        if (userProfileFragment == null) {
            userProfileFragment = new UserProfileFragment();
        }
        return userProfileFragment;
    }

    private String firstName = null, lastName = null, phoneNumber = null, email = null;
    final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_profile, null);
        mContext = getActivity();
        mPrefUtil = new PrefUtil(mContext);
        imageView_wallpaper_blur = (BannerView) view.findViewById(R.id.iv_wallpaper_blur);
        imageView_wallpaper = (ImageView) view.findViewById(R.id.iv_wallpaper);
        relativeLayout_wallpaperForeground = (RelativeLayout) view.findViewById(R.id.rl_wallpaperforeground);
        imageView_getImage = (ImageView) view.findViewById(R.id.iv_getimage);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.lv_accountsSettings);
        relativeLayoutFisrtname = (RelativeLayout) view.findViewById(R.id.rl_lockdetails_fisrtname);
        relativeLayoutLastName = (RelativeLayout) view.findViewById(R.id.rl_lockdetails_lastname);
        relativeLayoutPhoneNumber = (RelativeLayout) view.findViewById(R.id.rl_lockdetails_phonenumber);
        relativeLayoutEmail = (RelativeLayout) view.findViewById(R.id.rl_emailaddress);
        textView_label_profiledetails = (TextView) view.findViewById(R.id.tv_header_label);
        textView_label_firstName = (TextView) view.findViewById(R.id.tv_label_first_name);
        editText_firstName = (EditText) view.findViewById(R.id.et_first_name);
        textView_label_lastName = (TextView) view.findViewById(R.id.tv_label_last_name);
        editText_lastName = (EditText) view.findViewById(R.id.et_last_name);
        textView_label_phoneNumber = (TextView) view.findViewById(R.id.tv_label_phone_number);
        editText_phoneNumber = (EditText) view.findViewById(R.id.et_phone_number);
        textView_label_email = (TextView) view.findViewById(R.id.tv_label_email);
        editText_email = (EditText) view.findViewById(R.id.et_email);
        textView_accountSettings = (TextView) view.findViewById(R.id.tv_header_label_settings);
        textView_label_profiledetails.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_firstName.setTypeface(UtilHelper.getTypface(mContext));
        editText_firstName.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_lastName.setTypeface(UtilHelper.getTypface(mContext));
        editText_lastName.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_phoneNumber.setTypeface(UtilHelper.getTypface(mContext));
        editText_phoneNumber.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_email.setTypeface(UtilHelper.getTypface(mContext));
        editText_email.setTypeface(UtilHelper.getTypface(mContext));
        textView_accountSettings.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_profiledetails.setTypeface(UtilHelper.getTypface(mContext));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        isFacebooklogin = mPrefUtil.getBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
        mRecyclerView.setAdapter(new AccountSettingsMenuApater(mContext, title, userProfileFragment, isFacebooklogin));
        editText_firstName.setNextFocusDownId(R.id.et_last_name);
        editText_lastName.setNextFocusDownId(R.id.et_phone_number);// you can give focus to any id
        editText_phoneNumber.setNextFocusDownId(R.id.et_email);
        editText_firstName.setOnClickListener(this);
        editText_lastName.setOnClickListener(this);
        editText_phoneNumber.setEnabled(false);
        editText_email.setOnClickListener(this);
        relativeLayoutFisrtname.setOnClickListener(this);
        relativeLayoutLastName.setOnClickListener(this);
        relativeLayoutPhoneNumber.setOnClickListener(this);
        relativeLayoutEmail.setOnClickListener(this);
        textView_label_firstName.setOnClickListener(this);
        textView_label_lastName.setOnClickListener(this);
        textView_label_email.setOnClickListener(this);
        textView_label_phoneNumber.setOnClickListener(this);

        textView_label_firstName.setOnFocusChangeListener(this);

        textView_label_lastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.i("Tag", "b" + b);
                if (b) {
                    editText_lastName.setCursorVisible(true);
                    editText_lastName.requestFocus();
                }
            }
        });

        textView_label_email.setOnFocusChangeListener(this);
        textView_label_phoneNumber.setOnFocusChangeListener(this);
        if (isFacebooklogin) {
            editText_firstName.setFocusable(false);
            editText_lastName.setFocusable(false);
            editText_email.setFocusable(false);
            editText_firstName.setClickable(false);
            editText_lastName.setClickable(false);
            editText_email.setClickable(false);

        } else {
            editText_firstName.setFocusableInTouchMode(true);
            editText_lastName.setFocusableInTouchMode(true);
            editText_email.setFocusableInTouchMode(true);
            editText_email.addTextChangedListener(this);
            editText_firstName.addTextChangedListener(this);
            editText_lastName.addTextChangedListener(this);
        }
        imageView_getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted())
                    Crop.pickImage(getActivity());


            }
        });


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                File directory = cw.getDir("ellipes", Context.MODE_PRIVATE);
                Image = loadImageFromStorage(directory.getPath());
                if (Image != null) {
                    imageView_wallpaper_blur.setImageBitmap(blur(Image, 25));
                    imageView_wallpaper.setImageBitmap(Image);
                }
            }
        });


        editText_email.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editText_firstName.setCursorVisible(false);
                    editText_lastName.setCursorVisible(false);
                    editText_email.setCursorVisible(false);
                    return false;
                }
                return false;
            }
        });
        return view;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE_MEDIA);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE_MEDIA: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "Granted");
                    Crop.pickImage(getActivity());
                } else {
                    Log.e("Permission", "Denied");
                }
                return;
            }
        }
    }

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    @Override
    public void onStop() {
        if (isFieldChange) {
            getUserDetails();
        }
        try {
            InputMethodManager input = (InputMethodManager) getActivity()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        saveUserDetails();
        super.onPause();
    }

    public void saveUserDetails() {
        if (editText_firstName != null && editText_lastName != null && phoneNumber != null) {
            firstName = editText_firstName.getText().toString().trim();
            lastName = editText_lastName.getText().toString().trim();
            phoneNumber = editText_phoneNumber.getText().toString().trim();
            email = editText_email.getText().toString().trim();
            if (mUserRegistrationResponse.getPayload() != null) {
                mUserRegistrationResponse.getPayload().setFirst_name(firstName);
                mUserRegistrationResponse.getPayload().setLast_name(lastName);
                mUserRegistrationResponse.getPayload().setPhone_number(phoneNumber);
                mUserRegistrationResponse.getPayload().setEmail(email);
            }
            final String userResponseBeenJson = gson.toJson(mUserRegistrationResponse);
            mPrefUtil.setStringPref(SkylockConstant.PREF_USER_DETAILS, userResponseBeenJson);
        }
    }

    private void getUserDetails() {

        UpdateUserDetails.PropertiesEntity mPropertiesEntity = new UpdateUserDetails.PropertiesEntity();
        if (firstName != "" && !firstName.isEmpty())
            mPropertiesEntity.setFirst_name(firstName);
        if (lastName != "" && !lastName.isEmpty())
            mPropertiesEntity.setLast_name(lastName);
        mPropertiesEntity.setAddress1("");
        mPropertiesEntity.setCity("");
        mPropertiesEntity.setGender("");
        mPropertiesEntity.setMember_type("");
        mPropertiesEntity.setTitle("");
        mPropertiesEntity.setState("");
        if (email != "" && !email.isEmpty())
            mPropertiesEntity.setEmail(email);
        else
            mPropertiesEntity.setEmail(mPrefUtil.getStringPref(SkylockConstant.PREF_USER_EMAIL, email));
        mPropertiesEntity.setUser_id(mPrefUtil.getIntPref(SkylockConstant.PREF_USER_ID, 0));
        final String userResponseBeenJson = gson.toJson(mUserRegistrationResponse);
        mPrefUtil.setStringPref(SkylockConstant.PREF_USER_DETAILS, userResponseBeenJson);
        UpdateUserDetails updateUserDetails = new UpdateUserDetails();
        updateUserDetails.setProperties(mPropertiesEntity);
        //   if (!isLogout)
        updateUserDetails(updateUserDetails);

    }

    private void updateUserDetails(UpdateUserDetails updateUserDetails) {


        UserApiService UserApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<UserRegistrationResponse> mUpdateAccount = UserApiService.UpdateUserDetails(updateUserDetails);

        mUpdateAccount.enqueue(new Callback<UserRegistrationResponse>() {
            @Override
            public void onResponse(Call<UserRegistrationResponse> call, Response<UserRegistrationResponse> userRegistrationResponse) {

                if (userRegistrationResponse.code() == 200) {

                }
            }

            @Override
            public void onFailure(Call<UserRegistrationResponse> call, Throwable t) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomePageActivity) getActivity()).changeHeaderUI("MY PROFILE", ResourcesCompat.getColor(getResources(),
                R.color.colorPrimarylightdark, null), Color.WHITE);
        final String userdetailsJson = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_DETAILS, "");
        if (!userdetailsJson.equals("")) {
            Gson gson = new Gson();
            mUserRegistrationResponse = gson.fromJson(userdetailsJson, UserRegistrationResponse.class);
            if (mUserRegistrationResponse.getPayload() != null) {
                firstName = mUserRegistrationResponse.getPayload().getFirst_name();
                lastName = mUserRegistrationResponse.getPayload().getLast_name();
                phoneNumber = mUserRegistrationResponse.getPayload().getPhone_number();
                email = mUserRegistrationResponse.getPayload().getEmail();
            }
            if (!Objects.equals(firstName, "")) {
                editText_firstName.setText(firstName);
            }
            if (!Objects.equals(lastName, "")) {
                editText_lastName.setText(lastName);
            }
            if (!Objects.equals(phoneNumber, "")) {
                editText_phoneNumber.setText(phoneNumber);
            }
            if (!Objects.equals(email, "")) {
                if (email != null)
                    editText_email.setText(email);
                else
                    editText_email.setText(mPrefUtil.getStringPref(SkylockConstant.PREF_USER_EMAIL, email));
            }
        }


    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        isFieldChange = true;
    }

    public void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getActivity());
    }

    public void handleCrop(final int resultCode, final Intent result) {
        if (resultCode == getActivity().RESULT_OK) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    Uri sourceUri = Crop.getOutput(result);
                    try {

                        ExifInterface exif = new ExifInterface(sourceUri.getPath());
                        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int rotationInDegrees = exifToDegrees(rotation);
                        Matrix matrix = new Matrix();
                        if (rotation != 0f) {
                            matrix.preRotate(rotationInDegrees);
                        }
                        Bitmap Image = null;
                        BitmapRegionDecoder decoder = null;
                        try {

                            InputStream is = null;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;

                            is = getContext().getContentResolver().openInputStream(Crop.getOutput(result));

                            decoder = BitmapRegionDecoder.newInstance(is, false);

                            Image = decoder.decodeRegion(new Rect(0, 0, decoder.getWidth(), decoder.getHeight()), null);


                        } catch (OutOfMemoryError e) {
                            Image = decoder.decodeRegion(new Rect(800, 800, decoder.getWidth() - 800, decoder.getHeight() - 800), null);

                        } catch (IOException e) {
                        }


                        Bitmap adjustedBitmap = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix, true);

                        imageView_wallpaper_blur.setImageBitmap(blur(adjustedBitmap, 25));
                        imageView_wallpaper.setImageBitmap(adjustedBitmap);
                        saveToInternalStorage(adjustedBitmap);

                    } catch (IOException e) {
//                    android.util.Log.e("Error reading image: " + e.getMessage(), e);
//                    setResultException(e);
                    } catch (OutOfMemoryError e) {
//                    android.util.Log.e("OOM reading image: " + e.getMessage(), e);
//                    setResultException(e);
                    } finally {
                    }

                }
            });
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap blur(Bitmap original, float radius) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(getActivity());

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
                rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }

    public void deleteProfilePic() {
        try {
            isLogout = true;
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("ellipes", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, "profile.jpg");
            mypath.deleteOnExit();
            if (mypath.exists())
                mypath.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("ellipes", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String path) {
        Bitmap b = null;
        try {
            File file = new File(path, "profile.jpg");
            if (file.exists())
                b = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    public void closeLockConnection() {
        if (getActivity() != null)
            ((HomePageActivity) getActivity()).closeConnection();
    }

    @Override
    public void onClick(View v) {
        if (isFacebooklogin) {
            editText_phoneNumber.setCursorVisible(true);
        } else {
            switch (v.getId()) {
                case R.id.rl_lockdetails_fisrtname:
                case R.id.tv_label_first_name:
                case R.id.et_first_name:
                    editText_firstName.setCursorVisible(true);
                    editText_firstName.requestFocus();
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText_firstName, 0);

                    break;
                case R.id.tv_label_last_name:
                case R.id.rl_lockdetails_lastname:
                case R.id.et_last_name:
                    editText_lastName.setCursorVisible(true);
                    editText_lastName.requestFocus();
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText_lastName, 0);
                    break;
                case R.id.tv_label_email:
                case R.id.et_email:
                case R.id.rl_emailaddress:
                    editText_email.setCursorVisible(true);
                    editText_email.requestFocus();
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText_email, 0);

                    break;
            }
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UtilHelper.analyticTrackUserAction("Profile screen open", "Custom", "", null, "ANDROID");
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            switch (view.getId()) {
                case R.id.et_first_name:
                    editText_firstName.setCursorVisible(true);
                    editText_firstName.requestFocus();

                    break;
                case R.id.et_last_name:
                    editText_lastName.setCursorVisible(true);
                    editText_lastName.requestFocus();
                    break;
                case R.id.et_email:
                    editText_email.setCursorVisible(true);
                    editText_email.requestFocus();
                    break;
            }
        }
    }
}



