//
//  Parking.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 09/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import Model

public struct Parking {
    let spots: [Spot]
    let zones: [Zone]
    let hubs: [ParkingHub]
}


public extension Parking {    
    struct Check: Encodable {
        let fleetId: Int
        let latitude: Double
        let longitude: Double
    }
    
    enum Fee: Decodable {
        case allowed
        case fee(String)
        case outside
        case notAllowed
        
        public enum CodingKeys: String, CodingKey {
            case fee
            case currency
            case outside
            case notAllowed
        }
        
        public init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            if let not = try container.decodeIfPresent(Bool.self, forKey: .notAllowed), not {
                self = .notAllowed
            } else if let fee = try container.decodeIfPresent(Double.self, forKey: .fee),
                let currency = try container.decodeIfPresent(String.self, forKey: .currency),
                fee > 0,
                let price = fee.price(for: currency) {
                self = .fee(price)
            } else if let out = try container.decodeIfPresent(Bool.self, forKey: .outside), out {
                self = .outside
            } else {
                self = .allowed
            }
        }
    }
}
