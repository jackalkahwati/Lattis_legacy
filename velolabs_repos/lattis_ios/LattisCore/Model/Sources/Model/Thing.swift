//
//  Thing.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation

public struct Thing: Codable {
    public let controllerId: Int
    public let fleetId: Int
    public let make: String?
    public let model: String?
    public let key: String
    public let vendor: String
    public let deviceType: String
    public let batteryLevel: Int?
    public let qrCode: String?
    public let bikeId: Int?
}

public extension Thing {
    enum Vendor: String, Codable {
        case AXA
        case Segway
        case CoModule = "COMODULE"
        case CoModuleES4 = "COMODULE Ninebot ES4"
        case SegwayEU = "Segway IoT EU"
        case Noke
        case Kuhmute
        case Geotab = "Geotab IoT"
        case manual = "Manual Lock"
        case Linka = "Linka IoT"
        case tapkey = "Tap Key"
        case omni = "Omni"
        case acton = "ACTON"
        case Grow
        case Duckt
        case Kisi
        case ParcelHive = "ParcelHive"
        case Edge
        case Sas
        case Sentinel
    }
    
    enum DeviceType: String, Codable {
        case lock
        case iot
        case tracker
        case adapter
    }
    
    struct Status: Codable {
        public init(latitude: Double = 0, longitude: Double = 0, online: Bool = true, locked: Bool = true, batteryPercent: Double = 0, bikeBatteryPercent: Double = 0) {
            self.latitude = latitude
            self.longitude = longitude
            self.online = online
            self.locked = locked
            self.batteryPercent = batteryPercent
            self.bikeBatteryPercent = bikeBatteryPercent
        }
        
        public let latitude: Double
        public let longitude: Double
        public let online: Bool?
        public let locked: Bool
        public let batteryPercent: Double?
        public let bikeBatteryPercent: Double?
//        let lastUpdateTime: Date
    }
}

extension RawRepresentable where RawValue == String {
    public static func compare(rawValue: RawValue, to item: Self) -> Bool {
        guard let value = Self.init(rawValue: rawValue) else { return false }
        return value == item
    }
}
