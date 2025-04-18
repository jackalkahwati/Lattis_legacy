//
//  User.swift
//  Operator
//
//  Created by Ravil Khusainov on 04.04.2021.
//

import Foundation

struct User: Codable, Identifiable, Hashable, FullNameCodable {
    let id: Int
    let email: String
    let firstName: String?
    let lastName: String?
    let phoneNumber: String?
}
