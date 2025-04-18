//
//  Contact.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/8/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

struct Contact: Codable {
    var firstName: String?
    var lastName: String?
    var primaryNumber: String?
    var countryCode: String
    
    // Local
    var identifier: String? = nil
    var phoneNumbers: [String] = []
    
    var fullName: String {
        var name = firstName
        if let fn = name, fn.isEmpty == false {
            if let ln = lastName, ln.isEmpty == false {
                name = fn + " " + ln
            }
        } else {
            name = lastName
        }
        return name ?? ""
    }
}

extension Contact {
    enum CodingKeys: String, CodingKey {
        case lastName
        case firstName
        case primaryNumber = "phoneNumber"
        case countryCode
    }
    
    struct Emergency: Codable {
        let crashId: Int
        let contacts: [Contact]
    }
}

extension Contact: Equatable {}

func ==(lhs: Contact, rhs: Contact) -> Bool {
    return lhs.identifier == rhs.identifier
}
