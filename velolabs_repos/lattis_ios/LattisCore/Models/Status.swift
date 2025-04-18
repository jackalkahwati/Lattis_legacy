//
//  Status.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 02/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import Wrappers
import Model

extension Bike {
//    struct Booking {
//        let bike: Bike
//        let bookedOn: Date
//        let duration: TimeInterval
//        let tripService: TripViewModel
//
//        init(bike: Bike, bookedOn: Date, duration: TimeInterval) {
//            self.bike = bike
//            self.bookedOn = bookedOn
//            self.duration = duration
//            self.tripService = .init(bike)
//        }
//
//        var left: TimeInterval {
//            return duration + bookedOn.timeIntervalSinceNow
//        }
//    }
    
    struct Unbooking: Encodable {
        let bikeId: Int
        let bikeDamaged: Bool?
        let lockIssue: Bool?
        
        enum Reason {
            case damage
            case lockIssue
            case none
        }
    }
    
    func unbook(with reason: Unbooking.Reason = .none) -> Unbooking {
        switch reason {
        case .damage:
            return .init(bikeId: bikeId, bikeDamaged: true, lockIssue: nil)
        case .lockIssue:
            return .init(bikeId: bikeId, bikeDamaged: nil, lockIssue: true)
        default:
            return .init(bikeId: bikeId, bikeDamaged: nil, lockIssue: nil)
        }
    }
}

enum Status {
    case loading
    case search
    case booking(Bike.Booking, Bike)
    case trip(TripManager)
    case modern
    
    struct Info: Decodable {
        let trip: Trip?
        let booking: Booking?
        let operatorPhone: String?
        let supportPhone: String?
        let vehicle: Vehicle?
        let rating: Rating?
        let port: Rental.Port?
        let hub: Hub?
    }
    
    struct CurrentTrip: Codable {
        let tripId: Int
    }
    
    struct Booking: Codable {
        let bookingId: Int
        let bikeId: Int?
        let portId: Int?
        let hubId: Int?
        let till: Date
        let bookedOn: Date
    }
    
    struct Vehicle: Codable {
        let isDocked: Bool
    }
    
    struct Rating: Codable {
        let tripId: Int
    }
    
    @UserDefaultsBacked(key: "support.phone.number", defaultValue: UITheme.theme.support.phoneNumber)
    static var supportPhoneNumber: String
    
    @UserDefaultsBacked(key: "support.email", defaultValue: UITheme.theme.support.email)
    static var email: String
    
    @UserDefaultsBacked(key: "support.weblink")
    static var weblink: String?
    
    static func save(info: AppInfo) {
        supportPhoneNumber = info.phoneNumber
        email = info.email
        weblink = info.weblink
    }
}

extension Status.Info {
    
    static let none = Status.Info(trip: nil, booking: nil, operatorPhone: nil, supportPhone: nil, vehicle: nil, rating: nil, port: nil, hub: nil)
    
    enum CodingKeys: String, CodingKey {
        case trip
        case booking = "activeBooking"
        case operatorPhone = "onCallOperator"
        case supportPhone
        case vehicle
        case rating
        case port
        case hub
    }
}

