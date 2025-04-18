package cc.skylock.skylock;

//GOOGLE TURN BY TURN NAVIGATION
//Uri gmmIntentUri = Uri.parse("google.navigation:q=37.779378,-122.426083&mode=b");
//Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//mapIntent.setPackage("com.google.android.apps.maps");
//context.startActivity(mapIntent);

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexVijayRaj on 6/8/2015.
 */
public class MapActivity {

    Context context;
    GoogleMap map;
    ObjectRepo objRepo;
    Marker marker1, marker2, marker3, marker4, marker5, markerCycling;
    LocationManager locationManager;
    ImageView ivWalkingDirections;
    GoogleMap.InfoWindowAdapter infoWindowAdapter;
    List<LatLng> points = new ArrayList<LatLng>();
    int colorWalking = Color.rgb(110, 223, 158);
    int GET = 1;
    int POST = 2;
    int stateDirections = 0;
    List<Polyline> polylines = new ArrayList<Polyline>();
    private ProfilePictureView ivUserPic;
    private LatLng latlng = null;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public MapActivity(Context context1, GoogleMap map1, ObjectRepo objRepo1) {
        context = context1;
        map = map1;
        objRepo = objRepo1;

        initializeMapView();
           LatLng latlng = new LatLng(37.774929,-122.419416);
//        latlng = new LatLng(13.0695593, 80.2485111);
        setMarker("Alex", latlng, marker1);

    }

    private void initializeMapView() {

        //get location - zoom to location
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setMyLocationEnabled(true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    12));

            //if location is null go to the centre of San francisco
        } else {
            //San Francisco Coordinates
               LatLng latlng = new LatLng(37.774929,-122.419416);
            // chennai
//            LatLng latlng = new LatLng(13.0695593, 80.2485111);
            //13.0695593 80.2485111
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
                    12));
        }
        map.setMyLocationEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        //Turn off map rotation
        map.getUiSettings().setRotateGesturesEnabled(false);

        //set up info window
        infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.map_info_window, null);
                ivWalkingDirections = (ImageView) v.findViewById(R.id.ivWalkingDirections);


                return v;
            }
        };
        map.setInfoWindowAdapter(infoWindowAdapter);

        //info window onclick listener
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if (stateDirections == 0) {                                                  //checks if there is an ongoing walking direction
                    getWalkingDirections(marker.getPosition());                                     // get walking directions to the marker location
                    map.setMyLocationEnabled(true);
                    stateDirections = 1;
                } else {
                    objRepo.objRightNavDrawerAdapter.resetRightNavDrawer();
                    removeDirections();                                                      //if there is an ongoing walking direction, then remove it
                    stateDirections = 0;
                    map.setMyLocationEnabled(false);
                }
                if (marker.isInfoWindowShown()) {                                                   //closes the info window after the click
                    marker.hideInfoWindow();
                }
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setCyclingMarker(latLng, markerCycling);
                removeDirections();
                stateDirections = 0;
            }
        });

    }

    //sets marker on the location that is passed in
    //locality - just a name given to the marker - could be name of the lock
    //marker - totally 5 markers available - marker1, marker2, ... marker5
    public void setMarker(String locality, LatLng Location, Marker marker) {
        if (marker != null) {
            marker.remove();                                                                        //remove the marker if there is already one
        }
        // On marker click Listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                objRepo.rlPopUp.setVisibility(RelativeLayout.GONE);                                 //closes the relative layout containing the crash, theft and sharing icon
                objRepo.rlUpArrow.setVisibility(RelativeLayout.VISIBLE);                            //shows the more options relative layout
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow();                                                        //shows or closes the info window alternatively
                } else {
                    marker.hideInfoWindow();
                }
                return false;
            }
        });


        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(Location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_lock_icon_1));
        //  .icon(BitmapDescriptorFactory.fromBitmap(bmp));
        marker = map.addMarker(options);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(Location, 15);
        map.animateCamera(update);

    }

    public void setCyclingMarker(LatLng Location, Marker marker) {
        if (marker != null) {
            marker.remove();                                                                        //remove the marker if there is already one
        }
        // On marker click Listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                objRepo.rlPopUp.setVisibility(RelativeLayout.GONE);                                 //closes the relative layout containing the crash, theft and sharing icon
                objRepo.rlUpArrow.setVisibility(RelativeLayout.VISIBLE);                            //shows the more options relative layout
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow();                                                        //shows or closes the info window alternatively
                } else {
                    marker.hideInfoWindow();
                }
                return false;
            }
        });
        MarkerOptions options = new MarkerOptions()
                .title("Destination")
                .position(Location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_cycling_icon));
        markerCycling = map.addMarker(options);

    }

    //sets up the on click listener for the GPS on the home screen
    public void gpsOnClickListener() {
        //if walking directions is not enabled - turn off gps in 5 seconds
//        if (stateDirections == 0) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    map.setMyLocationEnabled(false);
//                }
//            }, 5000);
//        }
//        //else - move camera to the gps location
//        map.setMyLocationEnabled(true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = getLocation();
        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    16));
        }
    }

    public void getWalkingDirections(LatLng latLng) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?origin=");
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            centerMap(latLng, myLocation);
            String latOrigin = Double.toString(location.getLatitude());
            sb.append(latOrigin);
            sb.append(",");
            String longOrigin = Double.toString(location.getLongitude());
            sb.append(longOrigin);
            sb.append("&destination=");
            String latDest = Double.toString(latLng.latitude);
            sb.append(latDest);
            sb.append(",");
            String longDest = Double.toString(latLng.longitude);
            sb.append(longDest);
            sb.append("&mode=walking&key=");
            sb.append(context.getResources().getString(R.string.google_maps_key));
        }
        String urlString = sb.toString();
        objRepo.objJSON.putURL(urlString, GET, null, null);
        JSONObject jsonDirections = objRepo.objJSON.executeJSON();
        try {
            JSONArray jsonsteps = jsonDirections.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            String[] strPointsArray = new String[jsonsteps.length()];
            for (int i = 0; i < jsonsteps.length(); i++) {
                String strPoint = jsonsteps.getJSONObject(i).getJSONObject("polyline").getString("points");
                strPointsArray[i] = strPoint;
                drawLine(strPoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getCyclingDirections(String address) {
        removeDirections();
        map.setMyLocationEnabled(true);
        stateDirections = 1;
        StringBuilder sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?origin=");
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        if (location != null) {


            String latOrigin = Double.toString(location.getLatitude());
            sb.append(latOrigin);
            sb.append(",");
            String longOrigin = Double.toString(location.getLongitude());
            sb.append(longOrigin);
            sb.append("&destination=");
            sb.append("" + address);
            sb.append("&avoid=highways&mode=bicycling&key=");
            sb.append(context.getResources().getString(R.string.google_maps_key));
        }
        String urlString = sb.toString();
        objRepo.objJSON.putURL(urlString, GET, null, null);
        JSONObject jsonDirections = objRepo.objJSON.executeJSON();
        try {
            JSONArray jsonsteps = jsonDirections.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            String[] strPointsArray = new String[jsonsteps.length()];
            String[] strHTMLArray = new String[jsonsteps.length()];
            String[] strDistanceArray = new String[jsonsteps.length()];
            for (int i = 0; i < jsonsteps.length(); i++) {
                String strPoint = jsonsteps.getJSONObject(i).getJSONObject("polyline").getString("points");
                strPointsArray[i] = strPoint;
                drawLine(strPoint);

                String strDistance = jsonsteps.getJSONObject(i).getJSONObject("distance").getString("text");
                strDistanceArray[i] = strDistance;

                String s = jsonsteps.getJSONObject(i).getString("html_instructions");
                byte[] data = s.getBytes("ASCII");
                String s1 = new String(data);
                String strHTMLInstructions = Html.fromHtml(s1.replaceAll("<div.+?>", "\n")).toString();
                strHTMLArray[i] = strHTMLInstructions;
            }
            String totalDistance = jsonDirections.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
            objRepo.objRightNavDrawerAdapter.setDirections(jsonsteps.length(), totalDistance, strDistanceArray, strHTMLArray);
            objRepo.drawerListRight.setAdapter(objRepo.objRightNavDrawerAdapter);
            JSONObject jsonLatLng = jsonDirections.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("end_location");
            Double lat = jsonLatLng.getDouble("lat");
            Double lng = jsonLatLng.getDouble("lng");
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng latLng = new LatLng(lat, lng);
            setCyclingMarker(latLng, markerCycling);
            centerMap(latLng, myLocation);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //deletes all directions from the map
    public void removeDirections() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();

        objRepo.objRightNavDrawerAdapter.setDirections(0, "---", null, null);
        objRepo.drawerListRight.setAdapter(objRepo.objRightNavDrawerAdapter);

    }

    //takes in a poly string, decodes it using the decodePoly function and  plots the lines on the maps
    private void drawLine(String decodePolyString) {
        points = decodePoly(decodePolyString);                                                      //decodes the Polyline String from Google
        Polyline line = map.addPolyline(new PolylineOptions()
                .width(10)
                .color(colorWalking));
        line.setPoints(points);
        polylines.add(line);                                                                        //The line is added to the list to clear it in the end
    }

    //takes in two LatLngs, finds their centre spot and zooms camera to that level(for directions)
    private void centerMap(LatLng latlng1, LatLng latlng2) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latlng1);
        builder.include(latlng2);
        LatLngBounds bounds = builder.build();
        int padding = ((1000 * 10) / 100); // offset from edges of the map
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                padding);
        map.animateCamera(cu);
    }

    //decodes the encoded polyline from google maps and returns a list of LatLng's to be plotted on the map
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
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

