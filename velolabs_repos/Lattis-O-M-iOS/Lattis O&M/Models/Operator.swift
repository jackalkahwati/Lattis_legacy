//
//  Operator.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public struct Operator: Decodable {
    public let operatorId: Int
    public var firstName: String?
    public var lastName: String?
    public var email: String?
    public var phoneNumber: String?
    public var countryCode: String?
}

public extension Operator {
    var fullName: String {
        var name = firstName ?? ""
        if name.isEmpty {
            name = lastName ?? ""
        } else if let last = lastName {
            name += " \(last)"
        }
        return name
    }
    
    public struct LogIn: Encodable {
        let username: String
        let password: String
    }
}
