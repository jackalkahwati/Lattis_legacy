//
//  OvalBackend+Lattis.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import OvalBackend
import KeychainSwift
import Model


extension OvalBackend {
    
    init() {
        self.init(Env.ovalURL.absoluteString)
        self.userAgent = UITheme.theme.userAgent
        if let token = KeychainSwift().get("rest.token") {
            signIn(with: token)
        }
    }
    
    func createBooking(port: Hub.Port) async throws -> Booking {
        try await post(port.request, endpoint: .init(version: .v2, .bookings, path: "port"))
//        .init(bookingId: 234, supportPhone: "889732", onCallOperator: "88776", bookedOn: Date(), expiresIn: 15.minutes)
    }
    
    func cancel(booking: Booking, for asset: Asset) async throws {
        let _: EmptyJSON! = try await patch(asset.request, endpoint: .init(version: .v2, .bookings, path: "\(booking.bookingId)/cancel"))
    }
    
    func startTrip(with asset: Asset) async throws -> Trip {
//        .init(tripId: 77, bikeId: 234, fleetId: 55, startedAt: Date(), endedAt: nil, disableTracking: true, fare: nil, parkingFee: nil, surchargeFee: nil, penaltyFees: nil, totalPrice: 55, currency: "CAD", isStarted: true, logo: nil, fleetName: "BJJ", startAddress: nil, endAddress: nil, unlockFee: nil)
        struct Wrap: Codable {
            let trip: Trip
        }
        struct Rec: Decodable {
            let tripId: Int
        }
        let trip: Rec = try await post(asset.request, endpoint: .init(version: .v2, .trips, path: "start-trip"))
        let envelope: Wrap = try await post(["trip_id": trip.tripId], endpoint: .init(.trips, path: "get-trip-details"))
        return envelope.trip
    }
    
    func update(trip: Trip.Update) async throws -> Trip.Invoice {
        try await post(trip, endpoint: .init(.trips, path: "update-trip"))
    }
    
    func end<J: Encodable>(trip: J) async throws -> Trip {
        try await post(trip, endpoint: .init(.trips, path: "end-trip"))
    }
    
    func unlock(equipment: Int, asset: Asset) async throws {
        let _: EmptyJSON! = try await post(asset.request, endpoint: .init(.equipment, path: "\(equipment)/unlock"))
    }
    
    func lock(equipment: Int, asset: Asset) async throws {
        let _: EmptyJSON! = try await post(asset.request, endpoint: .init(.equipment, path: "\(equipment)/lock"))
    }
    
    func info(equipment: Int, asset: Asset) async throws -> Thing.Status {
        let query = asset.queryId
        let status: Thing.Status! = try await get(.init(.equipment, path: "\(equipment)/status", query: [.init(name: query.name, value: "\(query.value)")]))
        return status
    }
    
    func currentStatus() async throws -> Status.Info {
        try await post(UIDevice.current, endpoint: .init(.users, path: "get-current-status"))
    }
    
    func trip(_ tripId: Int) async throws -> Trip {
        try await post(["trip_id": tripId], endpoint: .init(.trips, path: "get-trip-details"))
    }
    
    func find(_ qrCode: String) async throws -> Rental {
        try await get(.init(.rentals, path: "find", query: [.init(name: "qr_code", value: qrCode)]))
    }
    
    func rate(_ tripId: Int, rating: Int) async throws {
        let _: EmptyJSON! = try await post(["trip_id": tripId, "rating": rating], endpoint: .init(.trips, path: "update-rating"))
    }
}

extension Hub.Port {
    struct Request: Codable {
        let portId: Int?
    }
    
    var request: Request {
        .init(portId: portId)
    }
    
//    struct Booking: Codable {
//        let bookingId: Int
//    }
}

extension Asset {
    struct Request: Codable {
        let portId: Int?
        let hubId: Int?
        let bikeId: Int?
    }
    
    var request: Request {
        switch self {
        case .port(let port, _):
            return .init(portId: port.portId, hubId: nil, bikeId: nil)
        case .bike(let bike):
            return .init(portId: nil, hubId: nil, bikeId: bike.bikeId)
        case .hub(let hub):
            return .init(portId: nil, hubId: hub.hubId, bikeId: nil)
        }
    }
}
