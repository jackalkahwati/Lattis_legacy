//
//  User.swift
//  
//
//  Created by Ravil Khusainov on 28.11.2020.
//

import Vapor
import Fluent

struct User: Content {
    let id: Int
    let scope: Scope
    let email: String
    let token: String
    let firstName: String?
    let lastName: String?
    let phoneNumber: String?
}

final class UserModel: Model {
    static var schema: String = "users"
    
    @ID(custom: "user_id")
    var id: Int?
    
    @OptionalField(key: "users_id")
    var identifier: String?
    
    @Enum(key: "user_type")
    var scope: User.Scope
    
    @OptionalField(key: "email")
    var email: String?
    
    @OptionalField(key: "password")
    var password: String?
    
    @OptionalField(key: "rest_token")
    var restToken: String?
    
    @OptionalField(key: "refresh_token")
    var refreshToken: String?
    
    @OptionalField(key: "first_name")
    var firstName: String?
    
    @OptionalField(key: "last_name")
    var lastName: String?
    
    @OptionalField(key: "phone_number")
    var phoneNumber: String?
    
    @OptionalField(key: "verified")
    var verified: Int?
    
    @OptionalField(key: "accepted_terms")
    var acceptedTerms: Int?
    
    @Timestamp(key: "date_created", on: .create, format: .unixUInt)
    var createdAt: Date?
    
    @OptionalField(key: "country_code")
    var countryCode: String?
}

extension User {
    enum Scope: String, Codable {
        case lattis, ellipse
    }
    
    init(_ model: UserModel) {
        id = model.id!
        scope = model.scope
        email = model.email!
        firstName = model.firstName
        lastName = model.lastName
        phoneNumber = model.phoneNumber
        token = model.restToken ?? "invalid-token"
    }
    
    final class FleetAccess: Model {
        static var schema: String = "private_fleet_users"
        
        @ID(custom: "private_fleet_user_id")
        var id: Int?
        
        @OptionalField(key: "user_id")
        var userId: Int?
    }
    
    final class PaymentProfile: Model {
        static var schema: String = "user_payment_profiles"
        
        @ID(custom: "id")
        var id: Int?
        
        @OptionalField(key: "user_id")
        var userId: Int?
    }
}
