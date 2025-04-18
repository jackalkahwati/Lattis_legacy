//
//  Geofence.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import CoreLocation

struct Geofence: Codable {
    let fleetId: Int
    let geofenceId: Int
    let name: String
    let geometry: Geomentry
}

extension Geofence {
    enum Shape: String, Codable {
        case polygon
        case rectangle
        case circle
    }
    struct Radius: Codable {
        let units: String
        let value: Double
        var metters: Double {
            value * 1000
        }
    }
    struct Point: Codable {
        let latitude: CLLocationDegrees
        let longitude: CLLocationDegrees
    }
    struct Geomentry: Codable {
        let shape: Shape
        let center: Point?
        let points: [Point]?
        let radius: Radius?
        let bbox: [CLLocationDegrees]?
    }
}

extension Geofence: MapShape {
    var polygonType: MapPolygonType { .geofence }
    var coordinates: [CLLocationCoordinate2D] {
        if let points = geometry.points {
            return points.map(CLLocationCoordinate2D.init)
        }
        if let center = geometry.center, let radius = geometry.radius?.metters {
            return .coordinates(center: CLLocationCoordinate2D(point: center), radius: radius)
        }
        if let bbox = geometry.bbox, bbox.count == 4 {
            return [
                CLLocationCoordinate2D(bbox[1], bbox[2]), //nw
                CLLocationCoordinate2D(bbox[3], bbox[2]), //ne
                CLLocationCoordinate2D(bbox[3], bbox[0]), //se
                CLLocationCoordinate2D(bbox[1], bbox[0])  //sw
            ]
        }
        return []
    }
}

extension CLLocationCoordinate2D {
    init(point: Geofence.Point) {
        self.init(latitude: point.latitude, longitude: point.longitude)
    }
}

extension Array where Element == CLLocationCoordinate2D {
    static func coordinates(center: CLLocationCoordinate2D, radius: Double) -> [CLLocationCoordinate2D] {
        let degreesBetweenPoints = 8.0
        //45 sides
        let numberOfPoints = floor(360.0 / degreesBetweenPoints)
        let distRadians: Double = radius / 6371000.0
        // earth radius in meters
        let centerLatRadians: Double = center.latitude * Double.pi / 180
        let centerLonRadians: Double = center.longitude * Double.pi / 180
        var coordinates = [CLLocationCoordinate2D]()
        //array to hold all the points
        for index in 0 ..< Int(numberOfPoints) {
            let degrees: Double = Double(index) * Double(degreesBetweenPoints)
            let degreeRadians: Double = degrees * Double.pi / 180
            let pointLatRadians: Double = asin(sin(centerLatRadians) * cos(distRadians) + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians))
            let pointLonRadians: Double = centerLonRadians + atan2(sin(degreeRadians) * sin(distRadians) * cos(centerLatRadians), cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians))
            let pointLat: Double = pointLatRadians * 180 / Double.pi
            let pointLon: Double = pointLonRadians * 180 / Double.pi
            let point: CLLocationCoordinate2D = CLLocationCoordinate2DMake(pointLat, pointLon)
            coordinates.append(point)
        }
        return coordinates
    }
}
