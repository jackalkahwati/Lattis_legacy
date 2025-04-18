//
//  User.swift
//  Lattis
//
//  Created by Ravil Khusainov on 21/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

struct User: Decodable {
    let userId: Int
    var email: String
    var isCurrent: Bool = true
    var firstName: String?
    var lastName: String?
    var phoneNumber: String?
    var privateNetworks: [PrivateNetwork] = []
    
    enum CodingKeys: String, CodingKey {
        case userId
        case email
        case firstName
        case lastName
        case phoneNumber
    }
}

enum Status {
    case none
    case find
    case booking(Int, Double, Double)
    case trip(Int)
    
    struct Info: Decodable {
        let status: Status
        let operatorPhone: String?
        let supportPhone: String?
        
        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            operatorPhone = try container.decodeIfPresent(String.self, forKey: .onCallOperator)
            supportPhone = try container.decodeIfPresent(String.self, forKey: .supportPhone)
            if let trip = try container.decodeIfPresent(Trip.self, forKey: .trip) {
                status = .trip(trip.tripId)
            } else if let booking = try container.decodeIfPresent(Booking.self, forKey: .activeBooking) {
                status = .booking(booking.bikeId, booking.bookedOn, booking.till)
            } else {
                status = .none
            }
        }
        
        struct Trip: Decodable {
            let tripId: Int
        }
        struct Booking: Decodable {
            let bikeId: Int
            let till: Double
            let bookedOn: Double
        }
        
        enum CodingKeys: String, CodingKey {
            case trip
            case activeBooking
            case onCallOperator
            case supportPhone
        }
    }
}

extension User {
    struct Password: Encodable {
        let confirmationCode: String
        let email: String
        let password: String
    }
    
    struct UpdatePassword: Encodable {
        let password: String
        let newPassword: String
    }
    
    struct Email: Encodable {
        let confirmationCode: String
        let email: String
    }
    
    struct Phone: Encodable {
        let phoneNumber: String
        let confirmationCode: String
    }
    
    struct Request: Encodable {
        let usersId: String
        let regId: String?
        let firstName: String?
        let lastName: String?
        let userType: String
        let password: String?
        let isSigningUp: Bool
        let email: String
        
        static func registration(email: String, firstName: String?, lastName: String?, password: String) -> Request {
            return .init(usersId: email, regId: AppDelegate.shared.pushToken, firstName: firstName, lastName: lastName, userType: "lattis", password: password, isSigningUp: true, email: email)
        }
        
        static func logIn(email: String, password: String) -> Request {
            return .init(usersId: email, regId: AppDelegate.shared.pushToken, firstName: nil, lastName: nil, userType: "lattis", password: password, isSigningUp: false, email: email)
        }
    }
    
    struct Tokens: Decodable {
        let restToken: String
        let refreshToken: String
    }
}

extension User.Request {
    init(_ user: User) {
        self.usersId = user.email
        self.firstName = user.firstName
        self.lastName = user.lastName
        self.email = user.email
        self.userType = "lattis"
        self.password = nil
        self.isSigningUp = false
        self.regId = nil
    }
}
