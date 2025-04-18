//
//  Device.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 29.10.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation

struct Device {
    enum Security: CaseIterable {
        case locked
        case unlocked
        case progress
        case undefined
    }

    enum Connection: CaseIterable {
        case disconnected
        case search
        case connecting
        case connected
    }
    
    enum Kind: CaseIterable {
        case ellipse
        case iot
        case axa
        case noke
        case adapter
        case manualLock
        case tapkey
        case omni
        case kisi
        case parcelHive
        case edge
    }
}

protocol DeviceRepresenting {
    var kind: Device.Kind { get }
    var security: Device.Security { get }
    var connection: Device.Connection { get }
    var consent: String? { get }
    var qrCode: String? { get }
    var bleRestricted: Bool { get }
    func lock()
    func unlock()
    func connect()
    func disconnect()
    func didSet(security: Device.Security)
    func refreshStatus()
}

extension DeviceRepresenting {
    func didSet(security: Device.Security) {}
}
