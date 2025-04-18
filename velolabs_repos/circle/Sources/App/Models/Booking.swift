//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 22.04.2021.
//

import Vapor
import Fluent

final class Booking: Model {
    static var schema: String = "booking"
    
    @ID(custom: "booking_id")
    var id: Int?
    
    @OptionalField(key: "booked_on")
    var startedAt: UInt?
    
    @OptionalField(key: "cancelled_on")
    var finishedAt: UInt?
    
    @Enum(key: "status")
    var status: Status
    
    @Field(key: "bike_id")
    var vehicleId: Int
    
    @Field(key: "user_id")
    var userId: Int
    
    @Field(key: "fleet_id")
    var fleetId: Int
    
    @OptionalField(key: "trip_id")
    var tripId: Int?
    
    @OptionalParent(key: "trip_id")
    var trip: TripModel?
}

extension Booking {
    enum Status: String, Codable {
        case active
        case cancelled
        case cancelled_with_issue
        case finished
    }
    
    struct Content: Vapor.Content {
        let id: Int
        let startedAt: UInt
        let finishedAt: UInt?
        let status: Status
        let user: User
    }
}

extension Booking.Content {
    init?(_ booking: Booking, users: [User]) {
        guard let id = booking.id, let startedAt = booking.startedAt, let user = users.first(where: {$0.id == booking.userId}) else { return nil }
        self.init(id: id, startedAt: startedAt, finishedAt: booking.finishedAt, status: booking.status, user: user)
    }
}
