//
//  Reservation.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation

public struct Reservation: Codable {
    public let reservationId: Int
    public let bikeId: Int
    public let userId: Int
    public let reservationStart: Date
    public let reservationEnd: Date
    public let reservationTimezone: String
    public let reservationCancelled: Date?
    public let createdAt: Date
    public let tripPaymentTransaction: Billing?
    public let bike: Bike
}

public extension Reservation {
    struct Estimate: Codable {
        public let amount: Double
        public let bikeId: Int
        public let chargeForDuration: Double
        public let currency: String
        public let duration: TimeInterval
        public let membershipDiscount: Double
    }
    
    struct Request: Codable {
        public let bikeId: Int
        public let reservationStart: Date
        public let reservationEnd: Date
        public let pricingOptionId: Int?
        
        public init(bikeId: Int, reservationStart: Date, reservationEnd: Date, pricingOptionId: Int?) {
            self.bikeId = bikeId
            self.reservationStart = reservationStart
            self.reservationEnd = reservationEnd
            self.pricingOptionId = pricingOptionId
        }
    }
    
    struct BikesRequest {
        public let fleetId: Int
        public let reservationStart: Date
        public let reservationEnd: Date
        
        public init(fleetId: Int, reservationStart: Date, reservationEnd: Date) {
            self.fleetId = fleetId
            self.reservationStart = reservationStart
            self.reservationEnd = reservationEnd
        }
    }
    
    struct Settings: Codable {
        public let minDuration: TimeInterval // PT1H
        public let fleetId: Int
        public let maxDuration: TimeInterval // PT10H,
        public let bookingWindowDuration: TimeInterval // P1M,
        public let id: Int
        public let deactivatedAt: Date?
        
        enum CodingKeys: String, CodingKey {
            case minDuration = "minReservationDuration"
            case fleetId
            case maxDuration = "maxReservationDuration"
            case bookingWindowDuration
            case id = "reservationSettingsId"
            case deactivatedAt = "deactivationDate"
        }
        
//        public static let stab = Settings(minDuration: 1.hours, fleetId: 0, maxDuration: 3.hours, bookingWindowDuration: 30.minutes, id: 0)
    }
    
    
    /// Pending Reservation
    struct Pending: Codable {
        public let reservationStart: String?
        public let reservationEnd: String?
        public let reservationTimezone: String?
        public let type: String?
        
        enum CodingKeys: String, CodingKey {
            case reservationStart = "reservationStart"
            case reservationEnd = "reservationEnd"
            case reservationTimezone = "reservationTimezone"
            case type
        }
    }
}

extension Reservation.Settings {
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        var durationString = try container.decode(String.self, forKey: .minDuration)
        
        minDuration = DateComponents.durationFrom8601String(durationString: durationString).duration
        
        durationString = try container.decode(String.self, forKey: .maxDuration)
        
        maxDuration = DateComponents.durationFrom8601String(durationString: durationString).duration
        
        durationString = try container.decode(String.self, forKey: .bookingWindowDuration)
        
        bookingWindowDuration = DateComponents.durationFrom8601String(durationString: durationString).duration
        fleetId = try container.decode(Int.self, forKey: .fleetId)
        id = try container.decode(Int.self, forKey: .id)
        deactivatedAt = try container.decodeIfPresent(Date.self, forKey: .deactivatedAt)
    }
}

//
//extension Reservation {
//    static let stab = Reservation(reservationId: 0, bikeId: 901, userId: 0, reservationStart: Date(), reservationEnd: Date().addingTimeInterval(2.hours), reservationTimezone: "", reservationCancelled: nil, createdAt: Date(), tripPaymentTransaction: .stab, bike: .stab)
//}
//
//extension Billing {
//    static let stab = Billing(id: 0, overUsageFees: 0, deposit: 0, chargeForDuration: 12, penaltyFees: 0, total: 12, bikeUnlockFee: 0, membershipDiscount: 0, currency: "USD", tripId: 0)
//}
//
//extension Bike {
//    static let stab = Bike(bikeId: 0, macId: nil, fleetKey: nil, name: "Some", fleetName: nil, fleetLogo: nil, picture: nil, skipParkingImage: nil, requirePhoneNumber: nil, latitude: 0, longitude: 0, kind: .electric, priceAmount: nil, parkingPriceAmount: nil, fleetType: nil, terms: nil, fleetId: 0, details: "", make: "", model: "", priceDuration: nil, pricePeriod: nil, currency: nil, excessUsageFees: nil, excessUsageDuration: nil, excessUsagePeriod: nil, excessUsageAfterPeriod: nil, excessUsageAfterDuration: nil, unlockFee: nil, controllers: nil, reservationSettings: nil, qrCodeId: nil, contactEmail: nil, customerName: nil)
//}
//
//extension Bike {
//    static let stab = Model.Bike(bikeGroup: .init(description: "", bikeGroupId: 0, operatorId: 0, fleetId: 0, type: .electric, pic: URL(string: "https://google.com")!, make: "", model: ""), bikeBatteryLevel: 0, fleet: .init(fleetId: 0, name: nil, email: nil, customer: nil, logo: nil, legal: "", type: .privateFree, reservationSettings: nil, address: nil), bikeName: "", bikeId: 0, latitude: 0, longitude: 0, qrCodeId: 0, macId: nil)
//}
