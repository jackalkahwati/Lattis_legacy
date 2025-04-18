//
//  Trip.swift
//  
//
//  Created by Ravil Khusainov on 15.03.2021.
//

import Vapor
import Fluent

struct Trip: Content {
    
    let id: Int
    let user: User
    let vehicleId: Int?
    let token: String?
    let steps: String?
    let createdAt: UInt?
    let endedAt: UInt?
    let receipt: Receipt?
    
    init?(_ trip: TripModel, users: [User]) {
        guard let user = users.first(where: {$0.id == trip.userId}) else { return nil }
        self.id = trip.id!
        self.user = user
        self.vehicleId = trip.vehicleId
        self.token = trip.token
        self.steps = nil
        self.createdAt = trip.createdAt
        self.endedAt = trip.endedAt
        self.receipt = trip.receipt
    }
}

final class TripModel: Model {
    
    static var schema: String = "trips"
    
    @ID(custom: "trip_id")
    var id: Int?
    
    @Field(key: "user_id")
    var userId: Int
    
    @Field(key: "bike_id")
    var vehicleId: Int?
    
    @OptionalField(key: "fleet_id")
    var fleetId: Int?
    
    @OptionalField(key: "device_token")
    var token: String?
    
    @OptionalField(key: "steps")
    var steps: String?
    
    @OptionalField(key: "date_created")
    var createdAt: UInt?
    
    @OptionalField(key: "date_endtrip")
    var endedAt: UInt?
    
    @OptionalField(key: "start_address")
    var startAddress: String?
    
    @OptionalField(key: "end_address")
    var endAddress: String?
    
    @OptionalChild(for: \.$trip)
    var receipt: Trip.Receipt?
    
    @OptionalChild(for: \.$trip)
    var booking: Booking?
}

extension Trip {
    final class Receipt: Model, Content {
        static var schema: String = "trip_payment_transactions"
        
        @ID(custom: "id")
        var id: Int?
        
        @Parent(key: "trip_id")
        var trip: TripModel
        
        @Field(key: "fleet_id")
        var fleetId: Int
        
        @OptionalField(key: "currency")
        var currency: String?
        
        @OptionalField(key: "bike_unlock_fee")
        var unlock: Double?
        
        @OptionalField(key: "over_usage_fees")
        var excess: Double?
        
        @OptionalField(key: "charge_for_duration")
        var mettered: Double?
        
        @OptionalField(key: "total")
        var total: Double?
        
        @OptionalField(key: "date_charged")
        var chargedAt: UInt?
        
        @OptionalField(key: "transaction_id")
        var transactionId: String?
    }
}

