//
//  ParkingSpot.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 29/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import CoreLocation
import Model

public extension Parking {
    struct Spot: Decodable {
        public let spotId: Int
        public let kind: Kind
        public let name: String
        public let details: String?
        public let pic: URL?
        let latitude: Double
        let longitude: Double
        
        public enum Kind: String, Decodable {
            case generic = "generic_parking"
            case parkingMeter = "parking_meter"
            case chargingSpot = "charging_spot"
            case bikeRack = "bike_rack"
            case dockingStation = "docking_station"
            case parkingRacks = "parking_racks"
            case sheffieldStand = "sheffield_stand"
            case locker
        }
        
        enum CodingKeys: String, CodingKey {
            case spotId = "parkingSpotId"
            case details = "description"
            case kind = "type"
            case name
            case latitude
            case longitude
            case pic
        }
        
        public var coordinate: CLLocationCoordinate2D {
            return .init(latitude, longitude)
        }
    }
}

extension Parking.Spot: MapPoint {
    public var identifier: String {
        switch kind {
        case .parkingMeter:
            return "annotation_parking_metter"
        case .bikeRack, .parkingRacks:
            return "annotation_parking_rack"
        case .chargingSpot:
            return "annotation_charging_spot"
        case .dockingStation:
            return "annotation_bike_hub"
        default:
            return "annotation_parking_spot"
        }
    }

    public var batteryLevel: Int? { nil }

    public var title: String? { nil }
    
    public var subtitle: String? { nil }
    
    public var color: UIColor { .azureRadiance }
    
    public var bage: Int? { nil }
    
    public func isEqual(to: MapPoint) -> Bool {
        guard let spot = to as? Parking.Spot else { return false }
        return spot.spotId == spotId && spot.coordinate == coordinate
    }
}

extension Parking.Spot: Equatable {
    
}

extension ParkingHub: MapPoint {
    public var coordinate: CLLocationCoordinate2D { .init(latitude, longitude)}
    
    public var identifier: String { "annotation_kuhmute_hub" }
    
    public var title: String? { hubName }
    
    public var subtitle: String? { nil }
    
    public var color: UIColor { .accent }
    
    public var bage: Int? { ports.count }
    
    public func isEqual(to: MapPoint) -> Bool {
        guard let hub = to as? ParkingHub else { return false }
        return hub.hubId == hubId && hub.coordinate == coordinate
    }

    public var batteryLevel: Int? { nil }
}
