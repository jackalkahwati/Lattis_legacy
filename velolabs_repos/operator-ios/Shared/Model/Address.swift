//
//  Address.swift
//  Operator
//
//  Created by Ravil Khusainov on 05.03.2021.
//

import Foundation

struct Address: Codable, Identifiable, Hashable {
    
    let id: Int
    let city: String?
    let address1: String?
    let address2: String?
    let state: String?
    let country: String?
    let postalCode: String?
}
