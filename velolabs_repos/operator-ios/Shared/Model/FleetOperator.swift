//
//  FleetOperator.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.02.2021.
//

import Foundation

struct FleetOperator: Codable, Identifiable, Hashable, FullNameCodable {
    let id: Int
    let email: String
    let firstName: String?
    let lastName: String?
    let phoneNumber: String?
}

extension FleetOperator {
    var isValid: Bool { self != .unassigned }
    
    struct LogIn: Encodable {
        let email: String
        let password: String
    }
    
    struct Auth: Decodable {
        let token: String
        let `operator`: FleetOperator
    }
}

protocol FullNameCodable {
    var firstName: String? { get }
    var lastName: String? { get }
}

extension FullNameCodable {
    var fullName: String {
        var name = firstName ?? ""
        if name.isEmpty {
            name = lastName ?? ""
        } else if let last = lastName {
            name += " " + last
        }
        return name.isEmpty ? "No Name" : name
    }
}
