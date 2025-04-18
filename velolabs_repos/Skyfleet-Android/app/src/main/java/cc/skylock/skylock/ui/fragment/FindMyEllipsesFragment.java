package cc.skylock.skylock.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cc.skylock.skylock.Bean.LockList;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.LockWebServiceApi;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.service.LocationService;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Velo Labs Android on 07-06-2016.
 */
public class FindMyEllipsesFragment extends Fragment implements GetLockInfo, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private View view;
    private GoogleMap map;
    private Context context;
    private Marker marker;
    private MarkerOptions markerOptions;
    private GoogleMap.InfoWindowAdapter infoWindowAdapter;
    private PrefUtil mPrefUtil;
    private LatLng currentLocation;
    private RelativeLayout lockUnlock_RelativeLayout, rl_progressbar, rl_routeLayout, rl_text_map_route_layout;
    private LinearLayout drawRouteBt;
    private TextView tv_lockname_bottom, tv_lockName, tv_timmings, tv_distance, lock_text_name_route, lock_test_route_distance,
            lock_test_route_time, lock_text_name_start_route_address, lock_route_steps, lock_text_name_end_route_address;
    private TextView tv_lockstatus_bottom, tv_label_get_direction;
    private ImageView imageView_small_lock, iv_text_route_layout_close;
    private String macID = null;
    private HomePageActivity homePageActivty;
    public static FindMyEllipsesFragment findMyEllipsesFragment;
    private boolean viewShown = false;
    private boolean settingFlag = false;
    private ImageView close_ImageView, imageView_loading;
    private Direction currentDirection;
    private Animation animation;
    private View mapView;
    private LocationService mLocationService;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_find_my_ellipses, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        context = getActivity();
        mPrefUtil = new PrefUtil(context);
        mLocationService = new LocationService(context);
        MapsInitializer.initialize(getActivity());
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map));
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        homePageActivty = (HomePageActivity) getActivity();
        tv_lockstatus_bottom = (TextView) view.findViewById(R.id.tv_lockstatus);
        tv_lockname_bottom = (TextView) view.findViewById(R.id.tv_lockName_bottom);
        imageView_small_lock = (ImageView) view.findViewById(R.id.iv_lock);
        tv_timmings = (TextView) view.findViewById(R.id.lock_status_route);
        tv_distance = (TextView) view.findViewById(R.id.lock_route_distance);
        drawRouteBt = (LinearLayout) view.findViewById(R.id.draw_route);
        lockUnlock_RelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_lockunlocklayout);
        rl_routeLayout = (RelativeLayout) view.findViewById(R.id.rl_map_route_layout);
        rl_text_map_route_layout = (RelativeLayout) view.findViewById(R.id.rl_text_map_route_layout);
        rl_progressbar = (RelativeLayout) view.findViewById(R.id.rl_progressbar);
        tv_lockName = (TextView) view.findViewById(R.id.lock_name_route);
        tv_label_get_direction = (TextView) view.findViewById(R.id.tv_label_get_direction);
        close_ImageView = (ImageView) view.findViewById(R.id.iv_route_layout_close);
        lock_text_name_route = (TextView) view.findViewById(R.id.lock_text_name_route);
        iv_text_route_layout_close = (ImageView) view.findViewById(R.id.iv_text_route_layout_close);
        lock_test_route_distance = (TextView) view.findViewById(R.id.lock_test_route_distance);
        lock_test_route_time = (TextView) view.findViewById(R.id.lock_test_route_time);
        lock_text_name_start_route_address = (TextView) view.findViewById(R.id.lock_text_name_start_route_address);
        lock_route_steps = (TextView) view.findViewById(R.id.lock_route_steps);
        lock_text_name_end_route_address = (TextView) view.findViewById(R.id.lock_text_name_end_route_address);
        imageView_loading = (ImageView) view.findViewById(R.id.iv_loading_progress);
        animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.rotate);
        tv_lockstatus_bottom.setVisibility(View.INVISIBLE);
        rl_progressbar.setVisibility(View.GONE);
        rl_routeLayout.setVisibility(View.GONE);
        tv_lockName.setTypeface(UtilHelper.getTypface(context));
        tv_lockname_bottom.setTypeface(UtilHelper.getTypface(context));
        tv_timmings.setTypeface(UtilHelper.getTypface(context));
        tv_distance.setTypeface(UtilHelper.getTypface(context));
        tv_lockstatus_bottom.setTypeface(UtilHelper.getTypface(context));
        lock_text_name_start_route_address.setTypeface(UtilHelper.getTypface(context));
        tv_label_get_direction.setTypeface(UtilHelper.getTypface(context));
        lock_text_name_route.setTypeface(UtilHelper.getTypface(context));
        lock_test_route_distance.setTypeface(UtilHelper.getTypface(context));
        lock_test_route_time.setTypeface(UtilHelper.getTypface(context));
        lock_text_name_end_route_address.setTypeface(UtilHelper.getTypface(context));
        lock_route_steps.setTypeface(UtilHelper.getTypface(context));
        if (homePageActivty != null) {
            homePageActivty.getHWInfo();
        }
        lockUnlock_RelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomePageActivity) getActivity()).clearBackStack();
            }
        });
        close_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_routeLayout.setVisibility(View.GONE);
            }
        });
        iv_text_route_layout_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rl_text_map_route_layout.setVisibility(View.GONE);
            }
        });
        drawRouteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_text_map_route_layout.setVisibility(View.VISIBLE);
                lock_test_route_distance.setText(currentDirection.getRouteList().get(0).getLegList().get(0).getDistance().getText() + " / ");
                lock_test_route_time.setText(currentDirection.getRouteList().get(0).getLegList().get(0).getDuration().getText() + getString(R.string.walk));
                lock_text_name_start_route_address.setText(currentDirection.getRouteList().get(0).getLegList().get(0).getStartAddress());
                String pathsepts = "";
                for (Step step : currentDirection.getRouteList().get(0).getLegList().get(0).getStepList()) {
                    pathsepts += step.getHtmlInstruction() + "\n";
                }

                lock_route_steps.setText(Html.fromHtml(pathsepts));
                rl_routeLayout.setVisibility(View.GONE);
                lock_text_name_end_route_address.setText(currentDirection.getRouteList().get(0).getLegList().get(0).getEndAddress());
                ArrayList<LatLng> directionPositionList = currentDirection.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                map.addPolyline(DirectionConverter.createPolyline(context, directionPositionList, 5, R.color.app_background));


            }
        });


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((HomePageActivity) getActivity()).getLockInfo = this;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewShown = true;
    }

    @Override
    public void onStop() {
        imageView_loading.clearAnimation();
        super.onStop();
    }

    private void intializeMap() {
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true);
            }
        } else {
            map.setMyLocationEnabled(true);
        }
        currentLocation = mLocationService.updateCoordinates();
        if (currentLocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,
                    16));
            getCurrentandLastLocation();
        }

    }

    @Override
    public void onResume() {
        ((HomePageActivity) getActivity()).changeHeaderUI("FIND MY ELLIPSE",
                Color.parseColor("#efefef"), ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimaryDark, null));
        ((HomePageActivity) context).getLockInfo = this;
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public boolean isShowing() {
        return viewShown;
    }

    private void getCurrentandLastLocation() {
        map.clear();
        try {
            if (LocationService.isLocationServiceEnabled(getActivity())) {
                if (HomePageActivity.mCurrentlyconnectedGatt != null) {
                    final LocationService location = new LocationService(context);
                    LatLng lockLocationLatLng = new LatLng(location.updateCoordinates().latitude, location.updateCoordinates().longitude);
                    System.out.println(" LATLONG " + lockLocationLatLng.latitude + " : " + lockLocationLatLng.longitude);
                    showMarker(lockLocationLatLng);

                } else {
                    String lockLocation = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LOCATION, null);
                    if (lockLocation != null) {
                        String[] lastLatLong = lockLocation.split(",");
                        LatLng lockLocationLatLng = new LatLng(Double.parseDouble(lastLatLong[0]), Double.parseDouble(lastLatLong[1]));
                        showMarker(lockLocationLatLng);
                    }
                }

            } else
                checkLocationListener();

        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }

    }

    private void showMarker(LatLng lockLocationLatLng) {
        markerOptions = new MarkerOptions()
                .position(lockLocationLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon));
        map.addMarker(markerOptions);
    }


    private void checkLocationListener() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            //   android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    public static FindMyEllipsesFragment newInstance() {
        if (findMyEllipsesFragment == null) {
            findMyEllipsesFragment = new FindMyEllipsesFragment();
        }
        return findMyEllipsesFragment;
    }

    @Override
    public void onGetHardwareInfo(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt != null) {

            macID = UtilHelper.getLockMacIDFromName(gatt.getDevice().getName());
            final String lockname = mPrefUtil.getStringPref(macID, "");
            final int position = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 4);

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String userdetailsJson = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_DETAILS, "");
                        final String timestamp = mPrefUtil.getStringPref(macID + SkylockConstant.LAST_CONNECTED_TIMESTAMP, "");
                        if (!userdetailsJson.equals("")) {
                            Gson gson = new Gson();
                            final UserRegistrationResponse lockList = gson.fromJson(userdetailsJson, UserRegistrationResponse.class);
                            final String firstName = lockList.getPayload().getLast_name();
                            if (!timestamp.equals("")) {
                                String time = UtilHelper.getDateCurrentTimeZone(Long.parseLong(timestamp), "hh:mm a 'on' MM/dd/yyyy");
                                String locked_by = "";

                                if (!TextUtils.isEmpty(firstName)) {
                                    locked_by = getString(R.string.locked_by_firstname, firstName, time);
                                } else {
                                    locked_by = getString(R.string.locked_by, time);
                                }

                                tv_timmings.setText(locked_by);
                            }
                        }
                        tv_lockstatus_bottom.setVisibility(View.VISIBLE);
                        if (position == 1 || position == 2) {
                            tv_lockstatus_bottom.setText(R.string.tap_to_unlock);
                            imageView_small_lock.setImageResource(R.drawable.icon_small_lock);
                        } else if (position == 0) {
                            tv_lockstatus_bottom.setText(R.string.tap_to_lock);
                            imageView_small_lock.setImageResource(R.drawable.icon_small_unlock);
                        }
                        rl_progressbar.setVisibility(View.GONE);
                        tv_lockname_bottom.setText(lockname);
                        tv_lockName.setText(lockname);

                    }
                });

            }
        }

    }

    @Override
    public void onGetBLESignal(BluetoothGatt gatt, final int value) {

    }

    @Override
    public void onBleDisconnect() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl_progressbar.setVisibility(View.GONE);
                    tv_lockname_bottom.setText(R.string.no_ellipse_connected);
                    imageView_small_lock.setImageResource(R.drawable.icon_small_lock);
                    tv_lockstatus_bottom.setVisibility(View.INVISIBLE);

                }
            });
        }

    }

    @Override
    public void onBoardFailed() {

    }

    @Override
    public void onConnectionTimeOut() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        intializeMap();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
        /*String lockLocation = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LOCATION, null);
        if (lockLocation != null && !lockLocation.equals("")) {
            String[] lockLatLong = lockLocation.split(",");
            Log.i("lockLatLong[0]", lockLatLong[0]);
            Log.i("lockLatLong[1]", lockLatLong[1]);
            LatLng lockLocationLatLng = new LatLng(Double.parseDouble(lockLatLong[0]), Double.parseDouble(lockLatLong[1]));
            markerOptions = new MarkerOptions()
                    .position(lockLocationLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon));
            googleMap.addMarker(markerOptions);
        }*/
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        map.clear();
        String lockLocation = mPrefUtil.getStringPref(SkylockConstant.PREF_LOCK_LOCATION, null);
        if (lockLocation != null) {
            String[] lockLatLong = lockLocation.split(",");
            LatLng lockLocationLatLng = new LatLng(Double.parseDouble(lockLatLong[0]), Double.parseDouble(lockLatLong[1]));
            markerOptions = new MarkerOptions()
                    .position(lockLocationLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon));
            map.addMarker(markerOptions);
        }
        GoogleDirection.withServerKey(getResources().getString(R.string.google_maps_key))
                .from(mLocationService.updateCoordinates())
                .to(marker.getPosition())
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            // Do something
                            Log.e("Direction", "Direction" + direction.getRouteList().get(0).getLegList().get(0).getDistance() + direction.getRouteList().get(0).getLegList().get(0).getDuration());
                            tv_distance.setText(direction.getRouteList().get(0).getLegList().get(0).getDistance().getText() + " " + direction.getRouteList().get(0).getLegList().get(0).getDuration().getText() + " walk");
                            rl_routeLayout.setVisibility(View.VISIBLE);
                            currentDirection = direction;

                        } else {
                            // Do something
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                    }
                });
        return true;
    }


    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


    private void getLockLis() {

        SkylockConstant.userToken = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_TOKEN, SkylockConstant.userToken);
        LockWebServiceApi lockWebServiceApi = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(LockWebServiceApi.class);
        Call<LockList> getLockList = lockWebServiceApi.GetLockData();
        getLockList.enqueue(new Callback<LockList>() {
            @Override
            public void onResponse(Call<LockList> call, Response<LockList> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus() == 200) {
                        LockList payloadEntity = response.body();
                        Gson gson = new Gson();
                        String lockJson = gson.toJson(payloadEntity);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, lockJson);
                        dispalyMylock(payloadEntity.getPayload().getUser_locks());


                    } else {
                        mPrefUtil.setStringPref(SkylockConstant.PREF_LOCK_LIST, "");

                    }
                }
            }

            @Override
            public void onFailure(Call<LockList> call, Throwable t) {
                Log.e("There are some problem", t.toString());
            }
        });
    }

    private void dispalyMylock(List<LockList.PayloadEntity.UserLocksEntity> myLocksEntitiesList) {

        for (LockList.PayloadEntity.UserLocksEntity myLocksEntity : myLocksEntitiesList) {
            if (currentLocation == null) {
                currentLocation = new LatLng(0, 0);

            }
            markerOptions = new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon));
            marker = map.addMarker(markerOptions);
            map.setInfoWindowAdapter(infoWindowAdapter);
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                }
            });

        }
        if (currentLocation != null)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,
                    16));
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UtilHelper.analyticTrackUserAction("Find my bike screen open", "Custom", "", null, "ANDROID");
    }
}
