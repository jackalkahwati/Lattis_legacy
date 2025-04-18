//
//  Fleet.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 08/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation

struct Fleet: Codable {
    let fleetId: Int
    let name: String?
    let email: String?
    let customer: String?
    let logo: URL?
}

extension Fleet {
    enum CodingKeys: String, CodingKey {
        case name = "fleetName"
        case email
        case customer = "customerName"
        case logo
        case fleetId
    }
}
