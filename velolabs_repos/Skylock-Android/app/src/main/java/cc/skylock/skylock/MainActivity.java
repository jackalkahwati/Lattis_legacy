package cc.skylock.skylock;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import cc.skylock.skylock.ui.Acivity.AddLockNew;
import cc.skylock.skylock.generator.HashGenerator;
import cc.skylock.skylock.network.NetworkUtil;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class MainActivity extends FragmentActivity implements LocationListener {

    ImageButton bMenu, bSettings, bMoreOptions, bLock, bLessOptions, bCrashAlert, bTheftAlert, bSharing, ibGPS, ibCyclingDirections;
    Button scanBluetooth;
    ObjectRepo objRepo;
    ImageView ivSignal;
    ListView drawerList, drawerListRight, bluetoothList;
    Context context = this;
    GoogleMap map;
    MapActivity objMapActivity;
    ProfileManager objProfileManager;
    Sharing objSharing;
    LocationManager locationManager;
    Settings objSettings;
    AddLock objAddLock;
    DrawerLayout mDrawerLayout;
    leftNavDrawerAdapter objLeftNavDrawerAdapter;
    RightNavDrawerAdapter objRightNavDrawerAdapter;
    AlertDialog.Builder builderPopUp;
    Dialog dialogLock;
    View viewPopUp;
    LayoutInflater inflater;
    RelativeLayout rlPopUp, rlUpArrow, rlGPS;
    BluetoothClass objBluetoothClass;
    BluetoothManager manager;
    List<BluetoothDevice> tmpBtChecker = null;
    BluetoothDevice Device;
    BluetoothGatt mBluetoothGatt;
    BluetoothAdapter myBluetoothAdapter;
    ArrayAdapter<String> BTArrayAdapter;
    BroadcastReceiver bReceiver;
    JSONClass objJSON;
    BackendClass objBackendClass;
    BackendTest objBackendTest;
    GoogleCloudMessaging gcm;
    String regid;
    Marker currentLocationMarker;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String PROJECT_NUMBER = "638979384071";
    Bitmap facebookProfilePic;
    HashGenerator objHashGenerator;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;
    BroadcastReceiver mRegistrationBroadcastReceiver, crout;
    IntentFilter filter1;
    LatLng currentLocation;

    BroadcastReceiver connectivityBroadcastReceiver;

    //String macAddress = "DA:31:F8:C5:1A:F0";
    //String macAddress = "F5:53:02:65:B8:53";
    //String macAddress = "D8:53:E3:3B:5A:FB";
    //String macAddress = "DC:E1:F2:CD:DE:35";
    //String macAddress = "FD:0E:6E:12:2F:BD";
    String macAddress = "E8:B4:2B:3A:D6:D3";
    RelativeLayout crouto_LinearLayout;
    TextView tv_croutonMessage;

    boolean canGetLocation = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crouto_LinearLayout = (RelativeLayout)findViewById(R.id.crouton_layout);
        tv_croutonMessage = (TextView)findViewById(R.id.textView_croutomessage);

        if (isNetworkBluetoothAvailable()) {
            init_setup();
            set_on_click_listeners();
        } else {
            showNoInternetDialog();
        }
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("cc.skylock.skylock.BROADCAST_RECEIVER")) {
                    Log.e("REG_ID", "" + intent.getStringExtra("message"));
                }
            }
        };
        filter1 = new IntentFilter("cc.skylock.skylock.BROADCAST_RECEIVER");

        registerReceiver(mRegistrationBroadcastReceiver, filter1);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "cc.skylock.skylock",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }




    private void init_setup() {
        //IDs
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        bMenu = (ImageButton) findViewById(R.id.bMenu);
        bSettings = (ImageButton) findViewById(R.id.bSettings);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerList = (ListView) findViewById(R.id.drawerList);
        drawerListRight = (ListView) findViewById(R.id.drawerListRight);
        bMoreOptions = (ImageButton) findViewById(R.id.bUpArrow);
        bLessOptions = (ImageButton) findViewById(R.id.bLessOptions);
        bLock = (ImageButton) findViewById(R.id.bLock);
        bCrashAlert = (ImageButton) findViewById(R.id.bCrashAlert);
        bTheftAlert = (ImageButton) findViewById(R.id.bTheftAlert);
        bSharing = (ImageButton) findViewById(R.id.bSharing);
        ibGPS = (ImageButton) findViewById(R.id.ibGPS);
        ibCyclingDirections = (ImageButton) findViewById(R.id.ibCyclingDirections);
        ivSignal = (ImageView) findViewById(R.id.ivSignal);
        builderPopUp = new AlertDialog.Builder(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewPopUp = inflater.inflate(R.layout.dialog, null);
        rlPopUp = (RelativeLayout) findViewById(R.id.rlPopUp);
        rlUpArrow = (RelativeLayout) findViewById(R.id.rlUpArrow);
        rlGPS = (RelativeLayout) findViewById(R.id.rlGPSButton);
        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        myBluetoothAdapter = manager.getAdapter();
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        crouto_LinearLayout.setVisibility(View.GONE);
        //Object Repository
        objRepo = new ObjectRepo(context);
        objRepo.bLock = bLock;
        objRepo.drawerList = drawerList;
        objRepo.drawerListRight = drawerListRight;
        objRepo.mDrawerLayout = mDrawerLayout;
        objRepo.objBluetoothClass = objBluetoothClass;
        objRepo.rlPopUp = rlPopUp;
        objRepo.rlUpArrow = rlUpArrow;
        objRepo.rlGPS = rlGPS;
        objRepo.ivSignal = ivSignal;
        objRepo.scanBluetooth = scanBluetooth;
        objRepo.bluetoothList = bluetoothList;
        objRepo.manager = manager;
        objRepo.tmpBtChecker = tmpBtChecker;
        objRepo.mBluetoothGatt = mBluetoothGatt;
        objRepo.Device = Device;
        objRepo.BTArrayAdapter = BTArrayAdapter;
        objRepo.myBluetoothAdapter = myBluetoothAdapter;
        objRepo.bReceiver = bReceiver;

        //Objects
        objHashGenerator = new HashGenerator(context, objRepo);
        objRepo.objHashGenerator = objHashGenerator;
        objBluetoothClass = new BluetoothClass(context, objRepo);
        objRepo.objBluetoothClass = objBluetoothClass;
        objJSON = new JSONClass(context);
        objRepo.objJSON = objJSON;
        objMapActivity = new MapActivity(context, map, objRepo);
        objRepo.objMapActivity = objMapActivity;
        objLeftNavDrawerAdapter = new leftNavDrawerAdapter(context);
        objRepo.objLeftNavDrawerAdapter = objLeftNavDrawerAdapter;
        objRightNavDrawerAdapter = new RightNavDrawerAdapter(context, objRepo);
        objRepo.objRightNavDrawerAdapter = objRightNavDrawerAdapter;
        objSharing = new Sharing(context, objRepo);
        objRepo.objSharing = objSharing;
        objProfileManager = new ProfileManager(context, objRepo);
        objRepo.objProfileManager = objProfileManager;
        objSettings = new Settings(context, objRepo);
        objRepo.objSettings = objSettings;
        objAddLock = new AddLock(context, objRepo);
        objRepo.objAddLock = objAddLock;
        objBackendClass = new BackendClass(context, objRepo);
        objRepo.objBackendClass = objBackendClass;
        objBackendTest = new BackendTest(context, objRepo);

        //Class - initializations
        drawerList.setAdapter(objRepo.objLeftNavDrawerAdapter);
        drawerListRight.setAdapter(objRepo.objRightNavDrawerAdapter);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.drawerListRight));

        //pop-up setup
        builderPopUp.setView(viewPopUp);
        dialogLock = builderPopUp.create();
        dialogLock.getWindow().setGravity(Gravity.BOTTOM);
        dialogLock.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogLock.setCanceledOnTouchOutside(true);
        dialogLock.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUp;
        rlPopUp.setVisibility(RelativeLayout.GONE);
        //rlGPS.setVisibility(RelativeLayout.GONE);

        //Bluetooth initialization
        objBluetoothClass.bluetooth_setup();

        //GCM
        if (checkPlayServices()) {
            Intent intent = new Intent(context, RegistrationIntentService.class);
            startService(intent);
            Log.i("REG___ID", "play services success");
        }
        //getRegId();

        Intent i = getIntent();

        String message = i.getStringExtra("message");
        //objRepo.displayToast(""+message);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    private void set_on_click_listeners() {

        //MapView Menu Button
        bMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.setScrimColor(Color.parseColor("#99000000"));
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        ibCyclingDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.setScrimColor(Color.TRANSPARENT);
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        //Mapview Settings Button
        bSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //mDrawerLayout.openDrawer(Gravity.RIGHT);
                objSettings.showSettings();
            }
        });

        //More options Button
        bMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialogLock.show();
                rlPopUp.setVisibility(RelativeLayout.VISIBLE);
                rlUpArrow.setVisibility(RelativeLayout.GONE);
            }
        });
        bLessOptions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                rlPopUp.setVisibility(RelativeLayout.GONE);
                rlUpArrow.setVisibility(RelativeLayout.VISIBLE);
            }
        });

        //Lock/Unlock Button
        bLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (objBluetoothClass.flagDeviceConnected == 1) {


                    //if state is locked
                    if (bLock.getTag().toString().equals("lock")) {
                        objBluetoothClass.pdLock = new ProgressDialog(context);                         //setup Lock progress dialog
                        objBluetoothClass.pdLock.setMessage("Locking...");
                        objBluetoothClass.pdLock.show();
                        try {
                            objBluetoothClass.unlock();

                        } catch (Exception e) {
                            Crouton.showText(MainActivity.this, R.string.no_device_info, Style.ALERT);
                            /*Toast.makeText(context, "No device is connected",
                                    Toast.LENGTH_LONG).show();*/
                        }
                    }
                    //if state is unlocked
                    else if (bLock.getTag().toString().equals("unlock")) {
                        objBluetoothClass.pdLock = new ProgressDialog(context);                         //setup Lock progress dialog
                        objBluetoothClass.pdLock.setMessage("Unlocking...");
                        objBluetoothClass.pdLock.setCancelable(false);
                        objBluetoothClass.pdLock.show();
                        try {
                            objBluetoothClass.lock();

                        } catch (Exception e) {
                            Crouton.showText(MainActivity.this, R.string.no_device_info, Style.ALERT);
                            /*Toast.makeText(context, "No device is connected",
                                    Toast.LENGTH_LONG).show();*/
                        }
                    }
                }else{
                    Crouton.showText(MainActivity.this, R.string.no_device_info, Style.ALERT);
                   /* Toast.makeText(context, "No device is connected",
                            Toast.LENGTH_LONG).show();*/
                }

            }
        });

        //Crash Alert Toggle Button
        bCrashAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (objBluetoothClass.flagDeviceConnected == 1) {
                    if (bTheftAlert.getTag().toString().equals("inactive")) {
                        //if state is active
                        if (bCrashAlert.getTag().toString().equals("active")) {
                            try {
                                objBluetoothClass.enableAcc(false, 0);//disable Crash
                                bCrashAlert.setImageResource(R.drawable.crash_alert_inactive);
                                bCrashAlert.setTag("inactive");
                            } catch (Exception e) {
                                Toast.makeText(context, "No device is connected",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                        //if state is inactive
                        else if (bCrashAlert.getTag().toString().equals("inactive")) {
                            try {
                                objBluetoothClass.enableAcc(true, 1); //enable Crash
                                bCrashAlert.setImageResource(R.drawable.crash_alert_inactive);
                                bCrashAlert.setTag("active");
                            } catch (Exception e) {
                                Toast.makeText(context, "No device is connected",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    } else {
                        Toast.makeText(context, "Turn off Theft Alert",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //Theft Alert Toggle Button
        bTheftAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (objBluetoothClass.flagDeviceConnected == 1) {
                    //if crash alert is off
                    if (bCrashAlert.getTag().toString().equals("inactive")) {
                        //if state is active
                        if (bTheftAlert.getTag().toString().equals("active")) {
                            try {
                                objBluetoothClass.enableAcc(false, 0); //disable Theft
                                bTheftAlert.setImageResource(R.drawable.theft_alert_inactive);
                                bTheftAlert.setTag("inactive");
                            } catch (Exception e) {
                                Toast.makeText(context, "No device is connected",
                                        Toast.LENGTH_LONG).show();
                            }
                            //if state is inactive
                        } else if (bTheftAlert.getTag().toString().equals("inactive")) {
                            try {
                                objBluetoothClass.enableAcc(true, 2); //enable theft
                                bTheftAlert.setImageResource(R.drawable.theft_alert_active);
                                bTheftAlert.setTag("active");
                            } catch (Exception e) {
                                Toast.makeText(context, "No device is connected",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                    } else {
                        Toast.makeText(context, "Turn off Crash Alert",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //Sharing Toggle Button
        bSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if state is active
                if (bSharing.getTag().toString().equals("active")) {
                    bSharing.setImageResource(R.drawable.sharing_inactive);
                    bSharing.setTag("inactive");

                }
                //if state is inactive
                else if (bSharing.getTag().toString().equals("inactive")) {
                    bSharing.setImageResource(R.drawable.sharing_active);
                    bSharing.setTag("active");

                }
            }
        });

        //Leftside Navigation drawer On click listener
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        objRepo.objProfileManager.showProfileManager();
                        //mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
//                    case 1:
//                        bluetoothConnect(macAddress);
//                        mDrawerLayout.closeDrawer(Gravity.LEFT);
//                        break;
                    case 1:
                        //mDrawerLayout.closeDrawer(Gravity.LEFT);
                        objRepo.objSharing.showSharing();
                        break;
                    case 2:
//                        objAddLock.showAddLock();
                        Intent showAddLockIntent =  new Intent(context, AddLockNew.class);
                        startActivity(showAddLockIntent);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 3:
                        //Intent i = new Intent("skylock.velo_labs.com.SQLVIEW");
                        //startActivity(i);
                        //objBackendTest.showBackendTest();

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.skylock.cc/"));
                        startActivity(browserIntent);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 4:

                        Intent browserIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.skylock.cc/pages/faq"));
                        startActivity(browserIntent1);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                }
            }
        });

        drawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        objBluetoothClass.bluetoothDisconnect();
                        break;
                }
                return true;
            }
        });

        ibGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Location location = getLocation();
                if (location != null) {
                    LatLng myLocation = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                            16));
                }
            }
        });


    }


   public void bluetoothConnect(String line2){


        BluetoothDevice device = objBluetoothClass.myBluetoothAdapter.getRemoteDevice("" + line2);
        if(device != null) {
            objBluetoothClass.setDevice(device);

            try {
                objBluetoothClass.bluetoothConnect();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    private boolean isNetworkBluetoothAvailable() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog(){
        Dialog dialog=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.setContentView(R.layout.splash_screen);
        dialog.setCancelable(false);
        dialog.show();

        showNoInternetAlertDialog();


    }

    private void showNoInternetAlertDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Network Issue" );
        alertDialog.setMessage("You're not connected to the internet. Please connect to the Internet and retry.");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (!isNetworkBluetoothAvailable()) {
                            showNoInternetAlertDialog();
                        } else {
                            Intent mainIntent = new Intent(context, MainActivity.class);
                            context.startActivity(mainIntent);
                        }
                    }
                });
        alertDialog.show();
    }

    private void getRegId(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM",  msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("Play-Services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        objRepo.objProfileManager.mCallBackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mRegistrationBroadcastReceiver);
        unregisterReceiver(connectivityBroadcastReceiver);

        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if(currentLocationMarker == null){
            new FaceBookImageTask().execute(ProfileManager.ivUserPic.getProfileId());
        }else{
            currentLocationMarker.setPosition(currentLocation);
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }
    public static Bitmap getFacebookProfilePicture(String userID){
        Bitmap bitmap = null;
        try{

            URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
              bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return bitmap;
    }
    class FaceBookImageTask extends AsyncTask<String, Void, Bitmap> {

        private Exception exception;

        protected Bitmap doInBackground(String... urls) {
            String userID = urls[0];
            try {
                URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
                Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                facebookProfilePic =Bitmap.createScaledBitmap(bitmap, 80, 80, false) ;
                return bitmap;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Bitmap feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
            View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
            ImageView fbProfilePic = (ImageView) marker.findViewById(R.id.facebook_profile_pic);
            fbProfilePic.setImageBitmap( facebookProfilePic);
            MarkerOptions mp = new MarkerOptions();

            mp.position(currentLocation);

            mp.title("my position");

            mp.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, marker)));
            if(facebookProfilePic != null){
                currentLocationMarker = map.addMarker(mp);
            }

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(cc.skylock.skylock.network.Network.isGPSenabledOrNot(context))
            Log.i("Gps","true");
        else
            Log.i("Gps","false");

        connectivityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == "android.net.conn.CONNECTIVITY_CHANGE") {
                    String status = NetworkUtil.getConnectivityStatusString(context);
                    if (status != null) {
                        //  Crouton.showText(MainActivity.this, status, Style.ALERT);
                        tv_croutonMessage.setText(status);
                        crouto_LinearLayout.setVisibility(View.VISIBLE);
                    }
                    else
                        crouto_LinearLayout.setVisibility(View.GONE);
                }
            }
          //  if()
        };
        registerReceiver(connectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }


    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

}