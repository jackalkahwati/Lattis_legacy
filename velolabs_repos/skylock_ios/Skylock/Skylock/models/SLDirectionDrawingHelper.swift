//
//  SLDirectionDrawingHelper.swift
//  Skylock
//
//  Created by Andre Green on 9/11/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

import Foundation
import MapKit
import CoreLocation
import GoogleMaps

class SLDirectionDrawingHelper: NSObject {
    let mapView: GMSMapView
    let directions: [SLDirection]
    var polyline: GMSPolyline?
    
    init(mapView: GMSMapView, directions: [SLDirection]) {
        self.mapView = mapView;
        self.directions = directions;
    }
    
    func drawDirections(_ completion: () -> ()) {
        let path = GMSMutablePath()
        for direction in self.directions where direction.start != nil {
            path.add(direction.start!)
        }
        
        if self.polyline != nil {
            self.polyline = nil
        }
        
        self.polyline = GMSPolyline(path: path)
        self.polyline!.strokeWidth = 5
        self.polyline!.strokeColor = UIColor.red
        self.polyline!.map = self.mapView
        
        completion()
    }
    
    func removeDirections() {
        if let polyline = self.polyline {
            polyline.map = nil
        }
    }
}
