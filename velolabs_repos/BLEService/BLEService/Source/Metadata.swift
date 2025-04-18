//
//  Metadata.swift
//  BLEService
//
//  Created by Ravil Khusainov on 8/15/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public extension Peripheral {
    public struct Metadata {
        public var batteryLevel: Double // 0 ~ 1
        public var batteryTemp: Int // in celcium
        public var rssi: Int
        public var lockState: Peripheral.LockState = .invalid
        
        init?(bytes: [UInt8]) {
            guard bytes.count > 4 else { return nil }
            let voltage = convert(bytes: Array(bytes[0...1]))
            if voltage > 3175 {
                self.batteryLevel = 1
            } else if voltage > 3050 {
                self.batteryLevel = 0.75
            } else if voltage > 2925 {
                self.batteryLevel = 0.5
            } else if voltage > 2800 {
                self.batteryLevel = 0.25
            } else {
                self.batteryLevel = 0
            }
            self.batteryTemp = Int(bytes[2])
            self.rssi = Int(bytes[3])
            if let state = Peripheral.LockState(rawValue: bytes[4]) {
                self.lockState = state
            }
        }
    }
}

func convert(bytes: [UInt8]) -> Float {
    if bytes.count == 2 {
        let array: [UInt8] = bytes.reversed()
        let uInt16Value = UInt16(array[0]) << 8 | UInt16(array[1])
        return Float(Int16(bitPattern: uInt16Value))
    }
    let data = Data(bytes: bytes)
    return Float(UInt32(littleEndian: data.withUnsafeBytes { $0.pointee }))
}
