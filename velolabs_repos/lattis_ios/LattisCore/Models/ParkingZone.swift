//
//  ParkingZone.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import CoreLocation

public extension Parking {
    struct Zone: Decodable {
        let zoneId: Int
        let shape: Shape
        let name: String?
        
        public init(from decoder: Decoder) throws {
            struct Geo: Decodable {
                let latitude: Double
                let longitude: Double
                let radius: Double?
            }
            
            func transform(_ geo: Geo) -> CLLocationCoordinate2D {
                return .init(latitude: geo.latitude, longitude: geo.longitude)
            }
            
            let container = try decoder.container(keyedBy: CodingKeys.self)
            self.zoneId = try container.decode(Int.self, forKey: .parkingAreaId)
            self.name = try container.decodeIfPresent(String.self, forKey: .name)
            let geometry = try container.decode([Geo].self, forKey: .geometry)
            let dots = geometry.map(transform)
            if let geo = geometry.first, let radius = geo.radius {
                let center = transform(geo)
                self.shape = .circle(.init(center: center, radius: radius))
            } else {
                let type = try container.decode(Kind.self, forKey: .type)
                switch type {
                case .polygon:
                    self.shape = .polygon(dots)
                case .rectangle:
                    self.shape = .rectangle(dots)
                }
            }
        }
        
        public enum CodingKeys: String, CodingKey {
            case parkingAreaId
            case name
            case geometry
            case type
        }
        
        private enum Kind: String, Decodable {
            case polygon
            case rectangle
        }
    }
}

public extension Parking.Zone {
    struct Circle {
        public let center: CLLocationCoordinate2D
        public let radius: Double
    }
    
    enum Shape {
        case polygon([CLLocationCoordinate2D])
        case circle(Circle)
        case rectangle([CLLocationCoordinate2D])
    }
}

extension Parking.Zone.Circle {
    var coordinates: [CLLocationCoordinate2D] {
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

extension Parking.Zone: MapShape {
    public var polygonType: MapPolygonType { .parking }
    public var coordinates: [CLLocationCoordinate2D] {
        switch shape {
        case .circle(let c):
            return c.coordinates
        case .polygon(let c), .rectangle(let c):
            return c
        }
    }
}
