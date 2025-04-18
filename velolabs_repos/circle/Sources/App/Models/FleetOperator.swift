//
//  FleetOperator.swift
//  
//
//  Created by Ravil Khusainov on 02.03.2021.
//

import Vapor
import Fluent

final class FleetOperator: Model, Authenticatable {
    static var schema: String = "operators"
    @ID(custom: "operator_id")
    var id: Int?
    
    @Field(key: "rest_token")
    var token: String
    
    @Field(key: "email")
    var email: String
    
    @Field(key: "password")
    var password: String
    
    @OptionalField(key: "first_name")
    var firstName: String?
    
    @OptionalField(key: "last_name")
    var lastName: String?
    
    @OptionalField(key: "phone_number")
    var phoneNumber: String?
}

extension FleetOperator {
    struct Auth: Content {
        let token: String
        let `operator`: User
    }
    
    struct Login: Content {
        let email: String
        let password: String
    }
    
    struct User: Content {
        let id: Int?
        let email: String
        let firstName: String?
        let lastName: String?
        let phoneNumber: String?
    }
}

extension FleetOperator.User {
    init(oper: FleetOperator) {
        id = oper.id
        email = oper.email
        firstName = oper.firstName
        lastName = oper.lastName
        phoneNumber = oper.phoneNumber
    }
}

