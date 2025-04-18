//
//  DeviceManager+Mock.swift
//  CoreTests
//
//  Created by Ravil Khusainov on 07.10.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
@testable import LattisCore

extension DeviceManager {
    static let iotOnly: DeviceManager = .init([IoTMockDevice()])
    static let ellipseOnly: DeviceManager = .init([EllipseMockDevice()])
    static let ellipseWithIot: DeviceManager = .init([IoTMockDevice(), EllipseMockDevice()])
}

class IoTMockDevice: DeviceRepresenting {
    var state: LockState = .disconnected
    
    var lockRequest: String? = "Tis should be requested before locking the IoT"
    
    func lock() {
        update(state: .locked, error: nil)
    }
    
    func unlock() {
        update(state: .unlocked, error: nil)
    }
    
    func connect() {
        update(state: .locked, error: nil)
    }
    
    func disconnect() {
        update(state: .disconnected, error: nil)
    }
    
    func update(state: LockState, error: Error?) {
        self.state = state
        send(state: state, error: error)
    }
}

class EllipseMockDevice: DeviceRepresenting {
    var state: LockState = .disconnected
    
    var lockRequest: String? = nil
    
    func lock() {
        update(state: .locked, error: nil)
    }
    
    func unlock() {
        update(state: .unlocked, error: nil)
    }
    
    func connect() {
        update(state: .locked, error: nil)
    }
    
    func disconnect() {
        update(state: .disconnected, error: nil)
    }
    
    func update(state: LockState, error: Error?) {
        self.state = state
        send(state: state, error: error)
    }
}
