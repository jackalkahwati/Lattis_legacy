//
//  Ellipse+BLE.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/17/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import LattisSDK

typealias Peripheral = LattisSDK.Ellipse

extension Ellipse {
    class Device {
        var ellipse: Ellipse?
        let peripheral: Peripheral
        
        init(_ peripheral: Peripheral) {
            self.peripheral = peripheral
            self.ellipse = nil
        }
        
        var macId: String {
            return peripheral.macId
        }
        
        var name: String {
            return ellipse?.name ?? peripheral.name
        }
    }
    
    class Lock {
        var ellipse: Ellipse
        var peripheral: Peripheral?
        init(_ ellipse: Ellipse, peripheral: Peripheral? = nil) {
            self.peripheral = peripheral
            self.ellipse = ellipse
        }
        
        var macId: String {
            return ellipse.macId
        }
        
        var name: String? {
            return ellipse.name ?? peripheral?.name
        }
        
        var isConnected: Bool {
            guard let per = peripheral else { return false }
            return per.isPaired
        }
        
        var connectedText: String? {
            guard let date = ellipse.connectedAt else { return nil }
            let formatter = DateFormatter()
            formatter.dateStyle = .medium
            formatter.timeStyle = .short
            return "last_connected_on_ios".localizedFormat(formatter.string(from: date))
        }
    }
}

extension Peripheral.Pin {
    init(_ pin: Ellipse.Pin) {
        switch pin {
        case .down:
            self = .down
        case .up:
            self = .up
        case .left:
            self = .left
        case .right:
            self = .right
        }
    }
}

extension Accelerometer.Sensetivity {
    init(_ value: Ellipse.Sensetivity) {
        switch value {
        case .low:
            self = .low
        case .medium:
            self = .medium
        case .high:
            self = .high
        }
    }
}
