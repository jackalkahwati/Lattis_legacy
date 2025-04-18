package com.lattis.ellipse.presentation.ui.bike;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.lattis.ellipse.Utils.ResourceUtil;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.Parking;
import com.lattis.ellipse.domain.model.ParkingZone;
import com.lattis.ellipse.domain.model.ParkingZoneGeometry;
import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.Utils.MapboxUtil.addMarker;
import static com.lattis.ellipse.Utils.MapboxUtil.addUserLocation;
import static com.lattis.ellipse.Utils.MapboxUtil.getUserLocationSymbol;
import static com.lattis.ellipse.Utils.MapboxUtil.unselected_size;
import static com.lattis.ellipse.Utils.MapboxUtil.zoomToMarkers;
import static com.lattis.ellipse.Utils.ResourceUtil.charging_spots;
import static com.lattis.ellipse.Utils.ResourceUtil.generic_parking;
import static com.lattis.ellipse.Utils.ResourceUtil.getResourcesByParkingType;
import static com.lattis.ellipse.Utils.ResourceUtil.parking_meter;
import static com.lattis.ellipse.Utils.ResourceUtil.parking_racks;
import static com.lattis.ellipse.Utils.ResourceUtil.user_location;
import static com.lattis.ellipse.presentation.ui.bike.FleetParkingZonePresenter.ARG_FLEET_ID;


public class FleetParkingActivity extends BaseCloseActivity<FleetParkingZonePresenter> implements
        OnMapReadyCallback, FleetParkingView {

    private MapboxMap mapboxMap;
    private List<LatLng> POLYGON_COORDINATES;
    private List<List<LatLng>> HOLE_COORDINATES = new ArrayList<List<LatLng>>();
    private List<Symbol> symbols = new ArrayList<>();
    private List<SymbolOptions> options = new ArrayList<>();
    private SymbolManager symbolManager;
    private FillManager fillManager;
    private LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
    private List<LatLng> latLngs = new ArrayList<>();

    @BindView(R.id.fleet_parking_mapView)
    MapView mapView;

    @Inject
    FleetParkingZonePresenter fleetParkingZonePresenter;


    public static void launchForResult(Activity activity, int fleet_id, int requestCode){
        Intent intent = new Intent(activity, FleetParkingActivity.class);
        intent.putExtra(ARG_FLEET_ID,fleet_id);
        activity.startActivityForResult(intent, requestCode);
    }


                                       @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected FleetParkingZonePresenter getPresenter() {
        return fleetParkingZonePresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_fleet_parking;
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.label_parking_zones));
        setToolBarBackGround(Color.WHITE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
        mapboxMap.getUiSettings().setCompassEnabled(false);

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                symbolManager = new SymbolManager(mapView,mapboxMap, style);
                symbolManager.setIconAllowOverlap(true);  //your choice t/f
                symbolManager.setTextAllowOverlap(true);  //your choice t/f
                fillManager = new FillManager(mapView,mapboxMap,style);

                Bitmap generic_parking_icon = BitmapFactory.decodeResource(getResources(), R.drawable.generic_parking_half);
                mapboxMap.getStyle().addImage(generic_parking,generic_parking_icon);

                Bitmap parking_meter_icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking_meter_half);
                mapboxMap.getStyle().addImage(parking_meter,parking_meter_icon);

                Bitmap parking_rack_icon = BitmapFactory.decodeResource(getResources(), R.drawable.parking_rack_half);
                mapboxMap.getStyle().addImage(parking_racks,parking_rack_icon);

                Bitmap charging_spot_icon = BitmapFactory.decodeResource(getResources(), R.drawable.charging_spot_half);
                mapboxMap.getStyle().addImage(charging_spots,charging_spot_icon);

                Bitmap user_location_icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_current_location_small);
                mapboxMap.getStyle().addImage(user_location,user_location_icon);


                getPresenter().getParkingZone();
            }
        });
    }

    @Override
    public void setUserPosition(Location location) {
        getPresenter().setCurrentUserLocation(location);
        Symbol userLocationSymbol = getUserLocationSymbol(symbols);
        if(userLocationSymbol==null){
            Symbol symbol =symbolManager.create(addUserLocation(getPresenter().getCurrentUserLocation().getLatitude(), getPresenter().getCurrentUserLocation().getLongitude(), ResourceUtil.getUserLocationResource(), unselected_size));
            symbols.add(symbol);
            LatLng currentLatLng = new LatLng(getPresenter().getCurrentUserLocation().getLatitude(),getPresenter().getCurrentUserLocation().getLongitude());
            latLngBounds.include(currentLatLng);
            latLngs.add(currentLatLng);
            adjustZoom();
        }else{
            userLocationSymbol.setLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            symbolManager.update(userLocationSymbol);
        }
    }


    ////////////////////////// code to show parking zone: start ////////////////////////////

    @Override
    public void onFindingZoneSuccess(List<ParkingZone> parkingZones) {
        mapboxMap.clear();
        HOLE_COORDINATES.clear();
        POLYGON_COORDINATES = new ArrayList<LatLng>() {
            {
                add(new LatLng(-90, -180));
                add(new LatLng(90, -180));
                add(new LatLng(90, 180));
                add(new LatLng(-90, 180));
                add(new LatLng(-90, -180));
            }
        };

        for (int i = 0; i < parkingZones.size(); i++) {
            ParkingZone parkingZone = parkingZones.get(i);
            if(parkingZone!=null ){
                List<ParkingZoneGeometry> parkingZoneGeometries = parkingZone.getParkingZoneGeometry();
                if(parkingZoneGeometries!=null && parkingZoneGeometries.size()>0){
                    List<LatLng> polygon = new ArrayList<>();

                    if(parkingZone.getType().equalsIgnoreCase("circle")){   //parkingzone is circular

                        for (int j = 0; j < parkingZoneGeometries.size(); j++) {
                            ParkingZoneGeometry parkingZoneGeometry = parkingZoneGeometries.get(j);
                            if (parkingZoneGeometry != null) {

                                LatLng centreLatLng = new LatLng();
                                centreLatLng.setLongitude(parkingZoneGeometry.getLongitude());
                                centreLatLng.setLatitude(parkingZoneGeometry.getLatitude());
                                double radius = parkingZoneGeometry.getRadius();

                                int degreesBetweenPoints = 8; //45 sides
                                int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
                                double distRadians = radius / 6371000.0; // earth radius in meters
                                double centerLatRadians = centreLatLng.getLatitude() * Math.PI / 180;
                                double centerLonRadians = centreLatLng.getLongitude() * Math.PI / 180;
                                for (int index = 0; index < numberOfPoints; index++) {
                                    double degrees = index * degreesBetweenPoints;
                                    double degreeRadians = degrees * Math.PI / 180;
                                    double pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
                                    double pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians) * Math.sin(distRadians) * Math.cos(centerLatRadians),
                                            Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians));
                                    double pointLat = pointLatRadians * 180 / Math.PI;
                                    double pointLon = pointLonRadians * 180 / Math.PI;
                                    LatLng latLng = new LatLng(pointLat, pointLon);


                                    latLngBounds.include(latLng);
                                    latLngs.add(latLng);
                                    polygon.add(latLng);
                                }


                            }
                        }

                    }else{  // parkingzone type is non circular

                        for (int j = 0; j < parkingZoneGeometries.size(); j++) {
                            ParkingZoneGeometry parkingZoneGeometry = parkingZoneGeometries.get(j);
                            if(parkingZoneGeometry!=null){
                                LatLng latLng = new LatLng();
                                latLng.setLongitude(parkingZoneGeometry.getLongitude());
                                latLng.setLatitude(parkingZoneGeometry.getLatitude());
                                polygon.add(latLng);
                                latLngBounds.include(latLng);
                                latLngs.add(latLng);
                            }
                        }
                    }

                    if(!polygon.isEmpty()){
                        HOLE_COORDINATES.add(polygon);
                    }
                }
            }
        }

        drawPolygon();
    }


    private void drawPolygon() {
        deleteAllPreviousPolygons();
        List<FillOptions> options = new ArrayList<>();
        for (List<LatLng> latLngList : HOLE_COORDINATES){
            List<List<LatLng>> latLngs = new ArrayList<>();
            latLngs.add(latLngList);
            FillOptions fillOptions = new FillOptions()
                    .withLatLngs(latLngs)
                    .withFillOutlineColor("#FF0000")
                    .withFillColor("#FF0000")
                    .withFillOpacity(0.1f);
            options.add(fillOptions);
        }
        fillManager.create(options);

    }

    private void deleteAllPreviousPolygons() {
        if(fillManager!=null) {
            fillManager.deleteAll();
        }
    }


    @Override
    public void onFindingParkingSuccess(List<Parking> parkings) {
        if (mapboxMap != null) {
            showParkings(parkings);
        }
    }

    private void showParkings(List<Parking> parkings) {
        clearMapView();
        if(parkings!=null && !parkings.isEmpty()) {
            for (Parking parking : parkings) {
                if (getPresenter().getFleetId() == parking.getFleet_id()) {
                    addMarker(options, parking.getLatitude(), parking.getLongitude(), getResourcesByParkingType(parking.getType().toUpperCase()), 1.0f);
                    LatLng parkingLatLng = new LatLng(parking.getLatitude(), parking.getLongitude());
                    latLngBounds.include(parkingLatLng);
                    latLngs.add(parkingLatLng);                }
            }
        }
        if(getPresenter().getCurrentUserLocation()!=null) {
            addUserLocation(options, getPresenter().getCurrentUserLocation().getLatitude(), getPresenter().getCurrentUserLocation().getLongitude(), ResourceUtil.getUserLocationResource(), unselected_size);
            LatLng currentLatLng = new LatLng(getPresenter().getCurrentUserLocation().getLatitude(),getPresenter().getCurrentUserLocation().getLongitude());
            latLngBounds.include(currentLatLng);
            latLngs.add(currentLatLng);
        }
        symbols = symbolManager.create(options);
        getPresenter().requestLocationUpdates();
    }

    @Override
    public void onFindingParkingFailure() {
        getPresenter().requestLocationUpdates();
    }


    public void clearMapView() {
        if (mapboxMap != null) {
            mapboxMap.clear();
            symbolManager.deleteAll();
            symbolManager.delete(symbols);
            symbols.clear();
            options.clear();
        }
    }

    private void adjustZoom(){
        zoomToMarkers(mapboxMap,latLngBounds,latLngs);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        deleteAllPreviousPolygons();
        clearMapView();
        getPresenter().requestStopLocationUpdates();
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
