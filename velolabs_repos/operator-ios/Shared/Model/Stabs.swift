//
//  Dummy.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.02.2021.
//

import Foundation


extension Array where Element == Fleet {
    static var dummy: Self {
        [
            .init(id: 1, name: "Castro District", email: nil, logo: nil, address: nil, vehiclesCount: 12),
            .init(id: 2, name: "Velo Transit", email: nil, logo: nil, address: nil, vehiclesCount: 10),
            .init(id: 3, name: "Stripe Test", email: nil, logo: nil, address: nil, vehiclesCount: 11)
        ]
    }
}

extension Fleet {
    static let select: Fleet = .init(id: 0, name: "Select Fleet", email: nil, logo: nil, address: nil, vehiclesCount: 0)
}

extension Array where Element == Ticket {
    static var dummy: Self {
        [
            .init(metadata: .init(id: 12, category: .damage_reported, status: .assigned, createdAt: Date(), assignee: 1, fleetId: 55, operatorId: 16, tripId: nil, vehicle: nil, operatorNotes: "some", riderNotes: "ham"), assignee: .ravil),
            .init(metadata: .init(id: 22, category: .issue_detected, status: .created, createdAt: Date(), assignee: nil, fleetId: 55, operatorId: nil, tripId: nil, vehicle: nil, operatorNotes: nil, riderNotes: nil), assignee: .unassigned),
            .init(metadata: .init(id: 24, category: .issue_detected, status: .created, createdAt: Date(), assignee: nil, fleetId: 55, operatorId: nil, tripId: nil, vehicle: nil, operatorNotes: nil, riderNotes: nil), assignee: .unassigned)
        ]
    }
}

extension FleetOperator {
    static let ravil = FleetOperator(id: 1, email: "ravil@lattis.io", firstName: "Ravil", lastName: "Khusainov", phoneNumber: "+79600448886")
    static let jeremy = FleetOperator(id: 2, email: "jeremy@lattis.io", firstName: "Jeremy", lastName: "Ricard", phoneNumber: nil)
    static let jack = FleetOperator(id: 3, email: "jack@lattis.io", firstName: "Jack", lastName: "Al-Kahwati", phoneNumber: nil)
    static let unassigned = FleetOperator(id: 0, email: "", firstName: "unassigned", lastName: nil, phoneNumber: nil)
}

extension Array where Element == FleetOperator {
    static var dummy: Self {
        [
            .ravil,
            .jeremy,
            .jack
        ]
    }
}

extension Array where Element == Thing.Metadata {
    static var dummy: Self {
        [
            .init(id: 1, key: "key", qrCode: nil, vendor: "Segway", make: nil, model: nil, deviceType: "IoT", latitude: nil, longitude: nil, fleetId: 44, fwVersion: nil, batteryLevel: 20),
            .init(id: 2, key: "key", qrCode: nil, vendor: "Geen", make: nil, model: nil, deviceType: "IoT", latitude: nil, longitude: nil, fleetId: 44, fwVersion: nil, batteryLevel: nil),
            .init(id: 3, key: "key", qrCode: nil, vendor: "AXA", make: nil, model: nil, deviceType: "lock", latitude: nil, longitude: nil, fleetId: 44, fwVersion: nil, batteryLevel: nil)
        ]
    }
}

extension Array where Element == Thing {
    static var dummy: Self { [Element.Metadata].dummy.map{Thing.init(metadata: $0)} }
}

extension Vehicle.Group {
    static var dummy: Self {
        .init(id: 0, type: .regular, make: nil, model: nil, description: nil, image: nil)
    }
}

extension Array where Element == Vehicle.Metadata {
    static var dummy: Self {
        [
            .init(id: 0, latitude: nil, longitude: nil, qrCode: nil, fleetId: 55, status: .active, usage: .parked, group: .dummy, batteryLevel: 23, ellipse: nil, things: []),
            .init(id: 1, latitude: nil, longitude: nil, qrCode: nil, fleetId: 55, status: .active, usage: .parked, group: .dummy, batteryLevel: nil, ellipse: nil, things: []),
            .init(id: 2, latitude: nil, longitude: nil, qrCode: nil, fleetId: 55, status: .active, usage: .parked, group: .dummy, batteryLevel: nil, ellipse: nil, things: []),
        ]
    }
}

extension Array where Element == Vehicle {
    static var dummy: Self { [Vehicle.Metadata].dummy.map{Vehicle.init($0)}  }
}
