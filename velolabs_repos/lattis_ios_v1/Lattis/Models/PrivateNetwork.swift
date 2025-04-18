//
//  PrivateNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/05/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

public struct PrivateNetwork: Codable {
    var email: String
    var logo: URL?
    var fleetId: Int
    var fleetName: String?
    var fleetUserId: Int
    var customerName: String?
    
    enum CodingKeys: String, CodingKey {
        case email
        case fleetId
        case fleetName
        case logo
        case customerName
        case fleetUserId = "privateFleetUserId"
    }
}

extension PrivateNetwork {
    var name: String {
        return fleetName ?? ""
//        var name = customerName ?? ""
//        if let fleetName = fleetName {
//            if name.isEmpty {
//                name = fleetName
//            } else {
//                name = "\(name) - \(fleetName)"
//            }
//        }
//        return name
    }
}
