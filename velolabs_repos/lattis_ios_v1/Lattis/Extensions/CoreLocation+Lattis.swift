//
//  CoreLocation+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import MapKit

extension CLLocationCoordinate2D {
    func isWithin(_ distance: Double, of coordinate: CLLocationCoordinate2D) -> Bool {
        guard CLLocationCoordinate2DIsValid(self) && CLLocationCoordinate2DIsValid(coordinate) else { return false }
        let oldPoint = MKMapPoint(self)
        let newPoint = MKMapPoint(coordinate)
        let realDistance = oldPoint.distance(to: newPoint)
        return abs(realDistance) < distance
    }
    
    func isOutOf(_ distance: Double, from coordinate: CLLocationCoordinate2D) -> Bool {
        guard CLLocationCoordinate2DIsValid(self) && CLLocationCoordinate2DIsValid(coordinate) else { return false }
        let oldPoint = MKMapPoint(self)
        let newPoint = MKMapPoint(coordinate)
        let realDistance = oldPoint.distance(to: newPoint)
        return abs(realDistance) > distance
    }
}

extension CLLocationCoordinate2D {
    struct Params: Codable {
        let latitude: Double
        let longitude: Double
    }
    
    var params: Params {
        return Params(latitude: latitude, longitude: longitude)
    }
}

extension CLLocationCoordinate2D: Equatable {}
public func ==(lhs: CLLocationCoordinate2D, rhs: CLLocationCoordinate2D) -> Bool {
    return lhs.latitude == rhs.latitude && lhs.longitude == rhs.longitude
}
