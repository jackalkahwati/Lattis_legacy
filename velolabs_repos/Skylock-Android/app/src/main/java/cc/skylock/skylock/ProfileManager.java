package cc.skylock.skylock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;

import java.util.ArrayList;


/**
 * Created by AlexVijayRaj on 8/12/2015.
 */
public class ProfileManager {

    Context context;
    Dialog dialogFBLogin, dialogProfile, dialogPhoneLogin, dialogSignupLogin, dialogLoginBackground;
    ObjectRepo objRepo;
    ImageButton ibClose, ibLoginPhone;
    leftNavDrawerAdapter objLeftNavDrawerAdapter;
    ListView drawerList;
    TextView tvPhone, tvEC, tvSignup1, tvSignup2, tvSignIn, tvForgotpassword;
    AlertDialog alertDialog;
    public CallbackManager mCallBackManager;
    private Profile profile;
    public static ProfilePictureView ivUserPic;
    AccessTokenTracker accessTokenTracker;
    EmergencyContacts objEmergencyContacts;
    Sharing objSharing;
    private ProfileTracker mProfileTracker;

    public ProfileManager(final Context context1, ObjectRepo objRepo1) {
        context = context1;
        objRepo = objRepo1;
        drawerList = objRepo.drawerList;
        objSharing = objRepo.objSharing;

        //FB initialize
        FacebookSdk.sdkInitialize(context);
        mCallBackManager = CallbackManager.Factory.create();

        //FB Profile page
        dialogProfile = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        //dialogProfile.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUp;
        dialogProfile.getWindow().getAttributes().windowAnimations = R.style.DialogZoom;
        dialogProfile.setContentView(R.layout.profile_page);
        tvPhone = (TextView) dialogProfile.findViewById(R.id.tvPhone);
        tvEC = (TextView) dialogProfile.findViewById(R.id.tvEC);
        ivUserPic = (ProfilePictureView) dialogProfile.findViewById(R.id.ivUserPic);
        ivUserPic.setPresetSize(-2);

        //Object for Emergency contacts
        objEmergencyContacts = new EmergencyContacts(context, objRepo, tvEC);
        objRepo1.objEmergencyContacts = objEmergencyContacts;

        //FB login dialog
        dialogFBLogin = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        dialogFBLogin.setContentView(R.layout.fb_login);
        dialogFBLogin.setCancelable(false);

        //Phone Login Screen
        dialogPhoneLogin = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        dialogPhoneLogin.setContentView(R.layout.fb_login1);
        dialogPhoneLogin.setCancelable(false);
        TextInputLayout textInputLayout_phonenumber = (TextInputLayout) dialogPhoneLogin.findViewById(R.id.input_layout_phonenumber);
        TextInputLayout textInputLayout_password = (TextInputLayout) dialogPhoneLogin.findViewById(R.id.input_layout_password);
        tvForgotpassword = (TextView) dialogPhoneLogin.findViewById(R.id.textView_forgetpwd);
        SpannableString content = new SpannableString("Forget Password");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvForgotpassword.setText(content);
        tvForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "Forget password clicked!", Toast.LENGTH_SHORT).show();
            }
        });


        //Phone Signup Screen
        dialogSignupLogin = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        dialogSignupLogin.setContentView(R.layout.fb_login2);
        dialogSignupLogin.setCancelable(false);
        TextInputLayout textInputLayout_firsName, textInputLayout_lastName, textInputLayout_phNum,
                textInputLayout_pwd, textInputLayout_cfmpwd;
        textInputLayout_firsName = (TextInputLayout) dialogSignupLogin.findViewById(R.id.input_layout_firstname);
        textInputLayout_lastName = (TextInputLayout) dialogSignupLogin.findViewById(R.id.input_layout_lastname);
        textInputLayout_phNum = (TextInputLayout) dialogSignupLogin.findViewById(R.id.input_layout_phNum);
        textInputLayout_pwd = (TextInputLayout) dialogSignupLogin.findViewById(R.id.input_layout_pwd);
        textInputLayout_cfmpwd = (TextInputLayout) dialogSignupLogin.findViewById(R.id.input_layout_cfmpwd);
        dialogLoginBackground = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        dialogLoginBackground.setContentView(R.layout.fb_login3);
        dialogLoginBackground.setCancelable(false);

        //sets up the facebook login button with access friends permission
        LoginButton loginButton = (LoginButton) dialogFBLogin.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        ibLoginPhone = (ImageButton) dialogFBLogin.findViewById(R.id.ibLoginPhone);
        tvSignup1 = (TextView) dialogFBLogin.findViewById(R.id.tvSignup);
        SpannableString content2 = new SpannableString("Not an user already? Sign Up");
        content2.setSpan(new UnderlineSpan(), 0, content2.length(), 0);
        tvSignup1.setText(content2);

        tvSignup2 = (TextView) dialogPhoneLogin.findViewById(R.id.tvSignup);
        SpannableString content1 = new SpannableString("Not an user already? Sign Up");
        content1.setSpan(new UnderlineSpan(), 0, content1.length(), 0);
        tvSignup2.setText(content1);


        tvSignIn = (TextView) dialogSignupLogin.findViewById(R.id.tvSignIn);
        SpannableString content3 = new SpannableString("Already have an account? Sign in");
        content3.setSpan(new UnderlineSpan(), 0, content3.length(), 0);
        tvSignIn.setText(content3);

        //sets up access token tracker - to determine user logout
        accessTokenTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {
                if (accessToken1 == null) {
                    dialogLoginBackground.show();
                    dialogFBLogin.show();
                }
            }
        };

        //callback for the facebook login button
        loginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {

            //on successful login
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = loginResult.getAccessToken();
                accessTokenTracker.startTracking();                                                 //starts tracking accesstokens to detect logout

                //Dismiss all Login Dialogs
                dialogFBLogin.dismiss();                                                            //dismisses the login screen
                dialogPhoneLogin.dismiss();
                dialogSignupLogin.dismiss();
                dialogLoginBackground.dismiss();

                profile = Profile.getCurrentProfile();
                if (profile != null) {                                                                //give profile(picture, name) to left nav drawer, profile screen
                    ivUserPic.setProfileId(profile.getId());
                    objRepo.objLeftNavDrawerAdapter.putProfile(profile);
                    objRepo.drawerList.setAdapter(objRepo.objLeftNavDrawerAdapter);
                    //objRepo.objBackendClass.createUser(profile.getFirstName(), profile.getLastName(), profile.getId(), profile.getName());
                    objRepo.fb_id = profile.getId();
                    fill_sharing();                                                                 //get facebook friends to populate sharing screen

                }
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile1, Profile profile2) {
                        profile = profile2;

                        if (profile != null) {                                                                //give profile(picture, name) to left nav drawer, profile screen
                            ivUserPic.setProfileId(profile.getId());
                            objRepo.objLeftNavDrawerAdapter.putProfile(profile);
                            objRepo.drawerList.setAdapter(objRepo.objLeftNavDrawerAdapter);
                            //objRepo.objBackendClass.createUser(profile.getFirstName(), profile.getLastName(), profile.getId(), profile.getName());
                            objRepo.fb_id = profile.getId();
                            fill_sharing();                                                                 //get facebook friends to populate sharing screen

                        }
                        mProfileTracker.stopTracking();
                    }
                };
                mProfileTracker.startTracking();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });

        //executes facebook stuff everytime the app opens
        profile = Profile.getCurrentProfile();
        if (profile == null) {
            dialogLoginBackground.show();
            dialogFBLogin.show();                                                                   //show login screen if no profile is found
        } else if (profile != null) {
            ivUserPic.setProfileId(profile.getId());                                                //give profile(picture, name) to left nav drawer, profile screen
            objRepo.objLeftNavDrawerAdapter.putProfile(profile);
            objRepo.fb_id = profile.getId();
            fill_sharing();                                                                         //get facebook friends to populate sharing screen


        }
        accessTokenTracker.startTracking();                                                         //starts tracking accesstokens to detect logout

        ibClose = (ImageButton) dialogProfile.findViewById(R.id.ibClose);                           //close button for profile page
        setOnClickListeners();

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogProfile.isShowing()) dialogProfile.dismiss();
            }
        });


    }

    private void setOnClickListeners() {
        //Alert dialog for setting up phone number
        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater li = LayoutInflater.from(context);
                View dialog_phone = li.inflate(R.layout.dialog_phone, null);
                final EditText etPhoneNumber = (EditText) dialog_phone.findViewById(R.id.etPhoneNumber);
                final ImageButton ibDismiss = (ImageButton) dialog_phone.findViewById(R.id.ibDismiss);
                final ImageButton ibDone = (ImageButton) dialog_phone.findViewById(R.id.ibDone);
                final TextInputLayout etPhoneNumber_TextInputLayout = (TextInputLayout) dialog_phone.findViewById(R.id.input_layout_etphonenumber);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(dialog_phone);
                ibDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String formattedNumber = PhoneNumberUtils.formatNumber(etPhoneNumber.getText().toString());
                        //detects if it is not empty
                        if (!formattedNumber.matches("")) {
                            //detects invalid phone number
                            if (formattedNumber.length() >= 10) {
                                tvPhone.setText(formattedNumber);
                                //objRepo.objBackendClass.addPhoneNumber(formattedNumber);
                            } else {
                                Toast.makeText(context, "Invalid Phone Number",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        alertDialog.cancel();
                    }
                });

                ibDismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });


                // create alert dialog
                alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });

        tvEC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                objEmergencyContacts.showAlertDialogEC();
            }
        });

        ibLoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoneLoginScreen();
            }
        });

        tvSignup1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignupScreen();
            }
        });

        tvSignup2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignupScreen();
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoneLoginScreen();
            }
        });
    }

    //shows the profile screen
    public void showProfileManager() {
        dialogProfile.show();

    }

    public void showPhoneLoginScreen() {
        dialogPhoneLogin.show();
        dialogFBLogin.dismiss();
        dialogSignupLogin.dismiss();
    }

    public void showSignupScreen() {
        dialogSignupLogin.show();
        dialogFBLogin.dismiss();
        dialogPhoneLogin.dismiss();
    }

    public String getFacebookId() {
        return profile.getId();
    }


    //get facebook friends and populate the sharing screen
    private void fill_sharing() {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        try {
                            int size = response.getJSONObject().getJSONArray("data").length();
                            objSharing.putFacebookFriendSize(size);                                 //send facebook friend size to sharing

                            ArrayList<String> list_name = new ArrayList<String>();
                            ArrayList<String> list_id = new ArrayList<String>();
                            String temp_name = null, temp_id = null;
                            for (int i = 0; i < size; i++) {
                                temp_name = response.getJSONObject().getJSONArray("data").getJSONObject(i).getString("name");
                                temp_id = response.getJSONObject().getJSONArray("data").getJSONObject(i).getString("id");
                                list_name.add(temp_name);
                                list_id.add(temp_id);
                            }
                            objSharing.putFacebookFriendNameList(list_name);                        //send facebook friend names
                            objSharing.putFacebookFriendIDList(list_id);                            //send facebook friend fb ids
                            objSharing.populateFacebookFriendList();                                //command to populate friend list
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }
}
