package com.lattis.ellipse.Utils;

import com.lattis.ellipse.domain.model.ParkingZone;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class InsideBoundaryUtils {
    List<ParkingZone> zoneList = new ArrayList<>();


    public static boolean checkPositonWithinBoundaries(LatLng currentLatlng, List<ParkingZone> list) {
        // TODO changed for mapbox
//        try {
//            for (ParkingZone zone : list) {
//                List<Position> polygonPositions = new ArrayList<>();
//                for (ParkingZoneGeometry geo : zone.getParkingZoneGeometry()) {
//                    polygonPositions.add(Position.fromCoordinates(
//                            geo.getLongitude(), geo.getLatitude()));
//                }
//                boolean pointWithin = TurfJoins.inside(Position.fromCoordinates(
//                        currentLatlng.getLongitude(), currentLatlng.getLatitude()), polygonPositions);
//                Log.i("pointWithin : ",""+pointWithin);
//                if (pointWithin)
//                    return pointWithin;
//            }
//            return false;
//        } catch (TurfException e) {
//            e.printStackTrace();
//            return false;
//        }
        return false;
    }


}

