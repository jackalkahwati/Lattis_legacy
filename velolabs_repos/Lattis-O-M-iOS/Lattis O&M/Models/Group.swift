//
//  Group.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/14/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public struct Group: Decodable {
    public let groupId: Int
    public let make: String
    public let model: String
    public let bikeType: BikeType
    
    public enum CodingKeys: String, CodingKey {
        case groupId = "bikeGroupId"
        case make
        case model
        case bikeType = "type"
    }
}

public extension Group {
    enum BikeType: String, Decodable {
        case regular
        case electric
        case kScooter = "Kick Scooter"
        case locker
        
        var display: String? {
            switch self {
            case .electric: return "E-bike"
            case .kScooter: return self.rawValue
            default: return nil
            }
        }
    }
    
    var display: String {
        var value = "\(make) \(model)"
        if let type = bikeType.display {
            value += ": \(type)"
        }
        return value
    }
}
