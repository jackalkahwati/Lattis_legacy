package com.lattis.ellipse.Utils;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

import static com.lattis.ellipse.Utils.ResourceUtil.ic_pick_location;
import static com.lattis.ellipse.Utils.ResourceUtil.user_location;

public class MapboxUtil {

    public static float unselected_size = 1.0f;
    public static float selected_size = 1.4f;

    public static void activateLocationComponent(Context context, MapboxMap mapboxMap, @NonNull Style loadedMapStyle) {

        // Check if permissions are enabled and if not request
//        if (PermissionsManager.areLocationPermissionsGranted(context)) {
//            LocationComponent locationComponent = mapboxMap.getLocationComponent();
//            // Activate with a built LocationComponentActivationOptions object
//            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, loadedMapStyle)
//                    .useDefaultLocationEngine(true)
//                    .build()
//            );
//        }
    }

    public static void disableLocationComponent(Context context, MapboxMap mapboxMap, @NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
//        if (PermissionsManager.areLocationPermissionsGranted(context)) {
//            LocationComponent locationComponent = mapboxMap.getLocationComponent();
//            locationComponent.setLocationComponentEnabled(false);
//        }
    }


    public static void enableLocationComponent(Context context, MapboxMap mapboxMap, @NonNull Style loadedMapStyle) {

        // Check if permissions are enabled and if not request
//        if (PermissionsManager.areLocationPermissionsGranted(context)) {
//
//            // Get an instance of the component
//            LocationComponent locationComponent = mapboxMap.getLocationComponent();
//
//            if(!locationComponent.isLocationComponentEnabled()) {
//                // Enable to make component visible
//                locationComponent.setLocationComponentEnabled(true);
//
//                // Set the component's camera mode
//                locationComponent.setCameraMode(CameraMode.NONE);
//
//                // Set the component's render mode
//                locationComponent.setRenderMode(RenderMode.NORMAL);
//            }
//
//        }
    }


    public static void boundCameraToRoute(DirectionsRoute currentRoute, LatLng bikeLng, MapboxMap mapboxMap ) {
        if (currentRoute != null) {
            List<Point> routeCoords = LineString.fromPolyline(currentRoute.geometry(),
                    Constants.PRECISION_6).coordinates();
            List<LatLng> bboxPoints = new ArrayList<>();
            for (Point point : routeCoords) {
                bboxPoints.add(new LatLng(point.latitude(), point.longitude()));
            }
            bboxPoints.add(new LatLng(bikeLng.getLatitude(),bikeLng.getLongitude()));
            if (bboxPoints.size() > 1) {
                try {
                    LatLngBounds bounds = new LatLngBounds.Builder().includes(bboxPoints).build();
// left, top, right, bottom
                    int bottomPadding = Math.round(convertDpToPixel(150)); // offset from edges of the map in pixels
                    int topPadding = Math.round(convertDpToPixel(80));
//                    animateCameraBbox(bounds, 500, new int[] {100, topPadding, bottomPadding, 50},mapboxMap);
                    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100,topPadding,100,bottomPadding));
                } catch (InvalidLatLngBoundsException exception) {

                }
            }
        }
    }

    public static void animateCameraBbox(LatLngBounds bounds, int animationTime, int[] padding, MapboxMap mapboxMap) {
        CameraPosition position = mapboxMap.getCameraForLatLngBounds(bounds, padding);
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationTime);
    }

    public static void addMarker(List<SymbolOptions> options,double latitude, double longitude, String imageStyle,float size) {
        LatLng latLng = new LatLng(latitude, longitude);
        // TODO add marker adding code
        options.add(new SymbolOptions()
                .withGeometry((Point) Feature.fromGeometry(Point.fromLngLat(longitude, latitude)).geometry())
                .withIconImage(imageStyle)
                .withZIndex(10)
                .withIconSize(size)
                .withIconOffset(new Float[] {0f,-size})
        );
        return;
    }

    public static void addUserLocation(List<SymbolOptions> options,double latitude, double longitude, String imageStyle,float size) {
        LatLng latLng = new LatLng(latitude, longitude);
        // TODO add marker adding code
        options.add(new SymbolOptions()
                .withGeometry((Point) Feature.fromGeometry(Point.fromLngLat(longitude, latitude)).geometry())
                .withIconImage(imageStyle)
                .withZIndex(15)
                .withIconSize(size)
                .withIconOffset(new Float[] {0f,-size})
        );
        return;
    }

    public static SymbolOptions addUserLocation(double latitude, double longitude, String imageStyle,float size) {
        LatLng latLng = new LatLng(latitude, longitude);
        return new SymbolOptions()
                .withGeometry((Point) Feature.fromGeometry(Point.fromLngLat(longitude, latitude)).geometry())
                .withIconImage(imageStyle)
                .withZIndex(15)
                .withIconSize(size)
                .withIconOffset(new Float[] {0f,-size});
    }


    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }


    public static void zoomToMarkers(MapboxMap mapboxMap, LatLngBounds.Builder builder, List<LatLng> latLngs) {

        if(latLngs!=null && latLngs.size()>1) { // build works for more than 1 item


            LatLngBounds bounds = null;

            try {
                bounds = builder.build();
            } catch (InvalidLatLngBoundsException ex) {
                setFixedZoomForSinglePoint(mapboxMap, latLngs);
                return;
            }

            // Calculate distance between northeast and southwest
            float[] results = new float[1];
            android.location.Location.distanceBetween(bounds.getNorthEast().getLatitude(), bounds.getNorthEast().getLongitude(),
                    bounds.getSouthWest().getLatitude(), bounds.getSouthWest().getLongitude(), results);

            CameraUpdate cu = null;
            if (results[0] < 1000) { // distance is less than 1 km -> set to zoom level 15
                cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15);
            } else {
                int bottomPadding = Math.round(convertDpToPixel(220)); // offset from edges of the map in pixels
                int topPadding = Math.round(convertDpToPixel(80));
                cu = CameraUpdateFactory.newLatLngBounds(bounds, 50, topPadding, 50, bottomPadding);
            }
            if (cu != null) {
                mapboxMap.animateCamera(cu,3000);
            }
        }else{
            setFixedZoomForSinglePoint(mapboxMap,latLngs);
        }
    }

    public static void setFixedZoomForSinglePoint(MapboxMap mapboxMap,List<LatLng> latLngs){
        if(latLngs!=null && latLngs.size()>0){
            CameraPosition cameraPosition =new CameraPosition.Builder()
                    .target(latLngs.get(0))
                    .zoom(15)
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition), 3000);


        }
    }

    public static boolean isNotSymbolOfuserLocation(Symbol symbol){
        return (!symbol.getIconImage().equals(user_location) && !symbol.getIconImage().equals(ic_pick_location)) ? true : false;
    }


    public static Symbol getUserLocationSymbol(List<Symbol> symbols){
        if(!symbols.isEmpty()){
            for(Symbol symbol:symbols){
               if(symbol.getIconImage().equals(user_location)){
                   return symbol;
               }
            }
        }
        return null;
    }


}
