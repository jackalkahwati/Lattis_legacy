//
//  Parking.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import CoreLocation

public struct Parking: Decodable {
    public let parkingId: Int
    public let coordinate: CLLocationCoordinate2D
    public var parkingType: ParkingType = .generic
    public var name: String?
    public var description: String? = nil
    public var pic: URL? = nil
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.parkingId = try container.decode(Int.self, forKey: .parkingSpotId)
        let lat = try container.decode(Double.self, forKey: .latitude)
        let lng = try container.decode(Double.self, forKey: .longitude)
        self.coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        self.name = try container.decodeIfPresent(String.self, forKey: .name)
        self.pic = try container.decodeIfPresent(URL.self, forKey: .pic)
        self.description = try container.decodeIfPresent(String.self, forKey: .description)
        if let type = try container.decodeIfPresent(ParkingType.self, forKey: .type) {
            self.parkingType = type
        } else {
            self.parkingType = .generic
        }
    }
    
    public enum CodingKeys: String, CodingKey {
        case parkingSpotId
        case latitude
        case longitude
        case name
        case description
        case pic
        case type
    }
}

extension Parking: AnnotationModel {
    var image: UIImage? {
        switch parkingType {
        case .parkingMeter:
            return #imageLiteral(resourceName: "icon_parking")
        case .bikeRack, .parkingRacks:
            return #imageLiteral(resourceName: "icon_bike_rack")
        case .chargingSpot:
            return #imageLiteral(resourceName: "icon_charging_spot")
        default:
            return #imageLiteral(resourceName: "icon_parking_spot")
        }
    }
}

public extension Parking {
    enum ParkingType: String, Codable {
        case generic = "generic_parking"
        case parkingMeter = "parking_meter"
        case chargingSpot = "charging_spot"
        case bikeRack = "bike_rack"
        
        case parkingRacks = "parking_racks"
        case sheffieldStand = "sheffield_stand"
        case locker
    }
    
    init(_ parkingId: Int, coordinate: CLLocationCoordinate2D, name: String? = nil) {
        self.parkingId = parkingId
        self.coordinate = coordinate
        self.name = name
    }
    
    enum Check: Decodable {
        case allowed
        case restricted
        case fee(Double, String)
        case outside
        
        public enum CodingKeys: String, CodingKey {
            case fee
            case currency
            case outside
            case notAllowed
        }
        
        public init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            if let notAllowed = try container.decodeIfPresent(Bool.self, forKey: .notAllowed), notAllowed {
                self = .restricted
            } else if let fee = try container.decodeIfPresent(Double.self, forKey: .fee),
                let currency = try container.decodeIfPresent(String.self, forKey: .currency),
                fee > 0 {
                self = .fee(fee, currency)
            } else if let out = try container.decodeIfPresent(Bool.self, forKey: .outside), out {
                self = .outside
            } else {
                self = .allowed
            }
        }
    }
}

func locationWithBearing(bearing:Double, distanceMeters:Double, origin:CLLocationCoordinate2D) -> CLLocationCoordinate2D {
    let distRadians = distanceMeters / (6372797.6) // earth radius in meters
    
    let lat1 = origin.latitude * Double.pi / 180
    let lon1 = origin.longitude * Double.pi / 180
    
    let lat2 = asin(sin(lat1) * cos(distRadians) + cos(lat1) * sin(distRadians) * cos(bearing))
    let lon2 = lon1 + atan2(sin(bearing) * sin(distRadians) * cos(lat1), cos(distRadians) - sin(lat1) * sin(lat2))
    
    return CLLocationCoordinate2D(latitude: lat2 * 180 / Double.pi, longitude: lon2 * 180 / Double.pi)
}

