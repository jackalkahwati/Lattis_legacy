//
//  Metadata.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation

public enum Metadata: Encodable {
    case jamming(Bike)
    case firmware(String, Bike)
    case lockBattery(Double, Bike)
    case bikeBattery(Double, Bike)
    
    public enum CodingKeys: String, CodingKey {
        case lockJammigCondition
        case lockFwVersion
        case bikeBatteryLevel
        case lockBatteryLevel
        case bikeId
        case lockId
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        func encode(bike: Bike) throws {
            try container.encode(bike.bikeId, forKey: .bikeId)
            try container.encode(bike.lockId, forKey: .lockId)
        }
        switch self {
        case .jamming(let bike):
            try encode(bike: bike)
            let bool: Bool = true
            try container.encode(bool, forKey: .lockJammigCondition)
        case let .firmware(version, bike):
            try encode(bike: bike)
            try container.encode(version, forKey: .lockFwVersion)
        case let .lockBattery(level, bike):
            try encode(bike: bike)
            try container.encode(Int(level*100), forKey: .lockBatteryLevel)
        case let .bikeBattery(level, bike):
            try encode(bike: bike)
            try container.encode(Int(level*100), forKey: .bikeBatteryLevel)
        }
    }
}
