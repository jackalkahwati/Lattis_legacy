//
//  ManualDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15.04.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import Model

struct ManualDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .manualLock
    let security: Device.Security = .locked
    let connection: Device.Connection = .connected
    let consent: String? = nil
    let qrCode: String? = nil
    let bleRestricted: Bool = false
    
    let thing: Thing
    
    init(_ thing: Thing) {
        self.thing = thing
    }
    
    func lock() { sendState() }
    
    func unlock() { sendState() }
    
    func connect() { sendState() }
    
    func disconnect() { sendState() }
    
    func refreshStatus() {
        
    }
}
