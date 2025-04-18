//
//  Metadata.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/27/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation

public extension Ellipse {
     struct Metadata {
        
        /// Power voltage in millivolts
        public var voltage: Float
        
        /// CPU temperature in Celcius (C)
        public var temperature: Double
        
        /// Signal streinght
        public var rssi: Double
        
        /// Security state
        public var security: Ellipse.Security = .invalid
        
        init?(bytes: [UInt8]) {
            guard bytes.count > 4 else { return nil }
            self.voltage = convert(bytes: Array(bytes[0...1]))
            self.temperature = Double(Int8(bitPattern:bytes[2]))
            self.rssi = Double(Int8(bitPattern: bytes[3]))
            if let state = Ellipse.Security(rawValue: bytes[4]) {
                self.security = state
            }
            print("Voltage:", voltage)
        }
        
        /// Battery level
        /// 0 - empty
        /// 1 - full
        /// Calculated using voltage with range 2.9V ~ 3.4V
        public var batteryLevel: Double {
            let range: (min: Double, max: Double) = (2900, 3400)
            let capacity = range.max - range.min
            let value: Double = Double(voltage) - range.min
            let result = max(min(value/capacity, 1), 0)
            return result
        }
        
        /// Signal level 0 ~ 1
        /// Calculated using rssi with rante -100Db ~ -50Db
        public var signalLevel: Double {
            let range: Double = 50
            let value: Double = rssi > -50 ? 0 : -rssi - 50
            let result = 1 - max(min(value/range, 1), 0)
            return result
        }
    }
}

func convert(bytes: [UInt8]) -> Float {
    if bytes.count == 2 {
        let array: [UInt8] = bytes.reversed()
        let uInt16Value = UInt16(array[0]) << 8 | UInt16(array[1])
        return Float(Int16(bitPattern: uInt16Value))
    }
    let data = Data(bytes)
    return Float(UInt32(littleEndian: data.withUnsafeBytes { $0.load(as: UInt32.self) }))
}
