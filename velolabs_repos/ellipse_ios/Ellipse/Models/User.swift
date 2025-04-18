//
//  User.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/10/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

enum UserType: String, Codable {
    case ellipse, facebook
}

struct User: Codable {
    let userId: Int
    var firstName: String?
    var lastName: String?
    var phone: String?
    var email: String?
    var usersId: String?
    
    // Local
    var pictureUrl: String? = nil
    var userType: UserType = .ellipse
    var locksCount: Int = 0
}

extension User {
    var fullName: String? {
        var name = firstName
        if let fn = name, fn.isEmpty == false {
            if let ln = lastName, ln.isEmpty == false {
                name = fn + " " + ln
            }
        } else {
            name = lastName
        }
        return name
    }
    
    var fullNameWithPhone: String? {
        var name = fullName
        if let nm = name, let ph = phone, nm.isEmpty == false {
            name = nm + " " + ph
        } else {
            name = phone
        }
        return name
    }
}

extension User {
    enum CodingKeys: String, CodingKey {
        case userId
        case firstName
        case lastName
        case phone = "phoneNumber"
        case email
        case usersId
        case userType
    }
}

extension User: Equatable {}

func ==(lhs: User, rhs: User) -> Bool {
    return lhs.firstName == rhs.firstName && lhs.lastName == rhs.lastName && lhs.email == rhs.email
}

