//
//  NokeDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 24.12.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import NokeMobileLibrary
import Model

final class NokeDeviceManager: DeviceRepresenting {
    
    let iot: Thing
    
    var kind: Device.Kind = .noke
    
    var security: Device.Security = .undefined
    
    var connection: Device.Connection = .disconnected
    
    var consent: String? = nil
    
    var qrCode: String?
    
    var bleRestricted: Bool = false
    
    fileprivate let device: NokeDevice
    
    fileprivate let manager = NokeMobileLibrary.NokeDeviceManager.shared()
    
    init(_ iot: Thing) {
        self.iot = iot
        self.qrCode = iot.qrCode
        self.device = NokeDevice(name: "some", mac: iot.key)!
        manager.setAPIKey("")
        manager.delegate = self
        manager.addNoke(device)
        manager.setAllowAllNokeDevices(true)
    }
    
    func lock() {
        // Noke can't be locked usint BLE
    }
    
    func unlock() {
        device.offlineUnlock()//unlock()
    }
    
    func connect() {
        if let state = device.connectionState, state == .Connected || state == .Connecting {
            return
        }
        manager.connectToNokeDevice(device)
    }
    
    func disconnect() {
        manager.disconnectNokeDevice(device)
    }
    
    func refreshStatus() {
        
    }
}

extension NokeDeviceManager: NokeDeviceManagerDelegate {
    func nokeDeviceDidUpdateState(to state: NokeDeviceConnectionState, noke: NokeDevice) {
        switch state {
        case .Discovered:
            manager.connectToNokeDevice(device)
        case .Connected:
            self.connection = .connected
        case .Connecting:
            self.connection = .connecting
        case .Disconnected:
            self.connection = .disconnected
            self.security = .undefined
        case .Unlocked:
            self.security = .unlocked
        default:
            break
        }
    }
    
    func nokeDeviceDidShutdown(noke: NokeDevice, isLocked: Bool, didTimeout: Bool) {
        
    }
    
    func nokeErrorDidOccur(error: NokeDeviceManagerError, message: String, noke: NokeDevice?) {
        
    }
    
    func didUploadData(result: Int, message: String) {
        
    }
    
    func bluetoothManagerDidUpdateState(state: NokeManagerBluetoothState) {
        bleRestricted = state != .poweredOn
    }
    
    func nokeReadyForFirmwareUpdate(noke: NokeDevice) {
        
    }
}
