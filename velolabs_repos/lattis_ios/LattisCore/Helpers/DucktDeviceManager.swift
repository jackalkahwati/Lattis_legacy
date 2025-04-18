//
//  DucktDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24.09.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import Model


final class DucktDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .adapter
    
    let security: Device.Security = .locked
    
    let connection: Device.Connection = .connected
    
    let consent: String? = nil
    
    let qrCode: String? = nil
    
    let bleRestricted: Bool = false
    
    let thing: Thing
    
    init(_ thing: Thing) {
        self.thing = thing
    }
    
    func lock() {
        sendState()
    }
    
    func unlock() {
        
    }
    
    func connect() {
        sendState()
    }
    
    func disconnect() {
        
    }
    
    func refreshStatus() {
        
    }
}
