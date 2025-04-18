//
//  Vehicle.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import Foundation
import CoreLocation

struct Vehicle: Codable, Identifiable {
    
    var metadata: Metadata
    let things: [Thing]
    
    var id: Int { metadata.id }
    var name: String { metadata.name ?? "No bike name"}
    var coordinate: CLLocationCoordinate2D? {
        guard let lat = metadata.latitude, let lon = metadata.longitude else { return nil }
        return .init(latitude: lat, longitude: lon)
    }
    
    init(_ metadata: Metadata) {
        self.metadata = metadata
        self.things = metadata.mapThings()
    }
    
    mutating func update(patch: Patch) {
        if let status = patch.status {
            metadata.status = status
        }
        if let usage = patch.usage {
            metadata.usage = usage
        }
        if let maintenance = patch.maintenance {
            metadata.maintenance = maintenance
        }
    }
}

extension Vehicle {
    struct Metadata: Codable, Identifiable {
        let id: Int
        var name: String?
        let latitude: CLLocationDegrees?
        let longitude: CLLocationDegrees?
        let qrCode: Int?
        let fleetId: Int
        var status: Status
        var usage: Usage
        var maintenance: Maintenance?
        let group: Group
        let batteryLevel: Double?
        let ellipse: Thing.EllipseLock?
        let things: [Thing.Metadata]?
    }
    
    struct Group: Codable {
        let id: Int
        let type: VehicleType
        let make: String?
        let model: String?
        let description: String?
        let image: URL?
    }
    
    enum Status: String, Codable, Identifiable {
        case active
        case inactive
        case suspended
        case deleted
        
        var id: Self { self }
    }
    
    enum Usage: String, Codable, Identifiable {
        case lock_assigned
        case controller_assigned
        case lock_not_assigned
        case parked
        case on_trip
        case damaged
        case reported_stolen
        case stolen
        case under_maintenance
        case total_loss
        case defleeted
        case defleet
        case collect
        case balancing
        case reserved
        case transport
        
        var id: Self { self }
    }
    enum Maintenance: String, Codable {
        case shop_maintenance
        case field_maintenance
        case parked_outside_geofence
        case issue_detected
        case damage_reported
        case reported_theft
    }
    
    enum VehicleType: String, Codable {
        case regular
        case kickScooter = "Kick Scooter"
        case electric
        case cart
        case locker
        case kayak
        case unknown
        
        var title: String {
            switch self {
            case .regular: return "Bike"
            case .electric: return "Electric Bike"
            case .kickScooter: return "Kickscooter"
            case .cart: return "Cart"
            case .locker: return "Locker"
            case .kayak: return "Kayak"
            case .unknown: return "Unknown"
            }
        }
    }
    
    struct Booking: Codable {
        let id: Int
        let startedAt: Date
        let finishedAt: Date?
        let tripId: Int?
        let user: User
    }
    
    struct TripMeta: Codable {
        let trips: [Trip]
        let history: Int
        let bookings: [Booking]
        
        func finishing(trip: Trip) -> TripMeta {
            var cache = trips
            if let idx = cache.firstIndex(of: trip) { cache.remove(at: idx) }
            var bookings = self.bookings
            if let idx = bookings.firstIndex(where: { $0.tripId == trip.id }) {
                bookings.remove(at: idx)
            }
            return .init(trips: cache, history: history + 1, bookings: bookings)
        }
    }
    
    struct Patch: Codable {
        let status: Status?
        let usage: Usage?
        let maintenance: Maintenance?
        let coordinate: CLLocationCoordinate2D?
    }
    
    var patch: Patch {
        .init(status: metadata.status, usage: metadata.usage, maintenance: metadata.maintenance, coordinate: coordinate)
    }
    
    enum Filter {
        case name(String)
        case usage([Usage])
        case batterLevel(Int)
        case maintenance([Maintenance])
        
        enum Maintenance: String {
            case lowBattery = "low-battery"
            case batteryLevel = "battery-level"
        }
    }
}

extension Vehicle: Hashable {
    static func == (lhs: Vehicle, rhs: Vehicle) -> Bool {
        lhs.hashValue == rhs.hashValue
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(metadata)
    }
}

extension Vehicle.Metadata: Hashable {
    static func == (lhs: Vehicle.Metadata, rhs: Vehicle.Metadata) -> Bool {
        lhs.hashValue == rhs.hashValue
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(name)
        hasher.combine(status)
        hasher.combine(usage)
        hasher.combine(maintenance)
    }
}

fileprivate extension Vehicle.Metadata {
    func mapThings() -> [Thing] {
        var things: [Thing] = []
        if let t = self.things {
            things = t.map{Thing(metadata: $0)}
        }
        if let lock = ellipse {
            things.append(.init(ellipse: lock))
        }
        return things
    }
}

extension Vehicle.Usage: Hashable {
    
}

extension Vehicle.Booking: Identifiable, Hashable {
    var duration: String? {
        let date = finishedAt ?? Date()
        let lengh = date.timeIntervalSince(startedAt)
        guard lengh > 0 else { return nil }
        let units: NSCalendar.Unit = lengh > 60 ? .standard : [.second]
        return lengh.duration(units)
    }
}

extension Vehicle.Filter: Equatable {
    
}
