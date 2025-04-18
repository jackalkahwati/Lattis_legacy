//
//  Metadata.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 8/15/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import LattisSDK

public enum Metadata: Encodable {
    case bike(EBikeInfo)
    case ellipse(Lock, Bool)
    case all(EBikeInfo, Lock)
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        switch self {
        case .bike(let info):
            try container.encode(Int(info.request.key), forKey: .bikeId)
            try container.encode(Int(info.batteryLevel*100), forKey: .bikeBatteryLevel)
        case .ellipse(let lock, let jamming):
            try container.encode(lock.lock?.lockId, forKey: .lockId)
            try container.encode(lock.lock?.bikeId, forKey: .bikeId)
            if let level = lock.peripheral?.metadata?.batteryLevel {
                try container.encode(Int(level*100), forKey: .lockBatteryLevel)
            }
            try container.encode(lock.peripheral?.firmwareVersion, forKey: .firmwareVersion)
            if jamming {
                try container.encode(jamming, forKey: .shackleJam)
            }
        case .all(let info, let lock):
            try container.encode(lock.lock?.lockId, forKey: .lockId)
            try container.encode(lock.lock?.bikeId, forKey: .bikeId)
            if let level = lock.peripheral?.metadata?.batteryLevel {
                try container.encode(Int(level*100), forKey: .lockBatteryLevel)
            }
            try container.encode(Int(info.batteryLevel*100), forKey: .bikeBatteryLevel)
        }
    }
    
    enum CodingKeys: String, CodingKey {
        case bikeId
        case bikeBatteryLevel
        case lockId
        case lockBatteryLevel
        case firmwareVersion
        case shackleJam
    }
}

public extension Metadata {
    var params: [String: Any] {
        switch self {
        case .bike(let info):
            return info.params
        case let .ellipse(lock, jam):
            return lock.params(jamming: jam)
        case .all(let info, let lock):
            return info.params + lock.params()
        }
    }
}

extension Lock {
    func params(jamming: Bool = false) -> [String: Any] {
        guard let lockId = lock?.lockId,
            let firmware = peripheral?.firmwareVersion,
            let metadata = peripheral?.metadata else { return [:] }
        var dict: [String: Any] = [
            "lock_id": lockId,
            "lock_battery_level": metadata.batteryLevel*100,
            "firmware_version": firmware,
            ]
        if jamming {
            dict["shackle_jam"] = true
        }
        if let bikeId = lock?.bikeId {
            dict["bike_id"] = bikeId
        }
        return dict
    }
}

func + <K,V>(left: Dictionary<K,V>, right: Dictionary<K,V>)
    -> Dictionary<K,V>
{
    var map = Dictionary<K,V>()
    for (k, v) in left {
        map[k] = v
    }
    for (k, v) in right {
        map[k] = v
    }
    return map
}
