//
//  EllipseDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 29.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import EllipseLock

final class EllipseDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .ellipse
    let consent: String? = nil
    var security: Device.Security = .undefined { didSet { sendState() }}
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    let qrCode: String? = nil
    var bleRestricted: Bool { !ble.isOn }
    
    let fleetKey: String
    let macId: String
    
    fileprivate var buffer: Device.Security?
    
    init(fleetKey: String, macId: String, bikeId: Int) {
        connectBikeId = bikeId
        self.fleetKey = fleetKey
        self.macId = macId
    }
    fileprivate let ble = EllipseManager.shared
    fileprivate var ellipse: Ellipse?
    fileprivate var jammingHappend = false
    fileprivate var reconnectTimer: Timer?
    
    func lock() {
        ellipse?.lock()
        security = .progress
    }
    
    func unlock() {
        ellipse?.unlock()
        security = .progress
    }
    
    func connect() {
        if let device = ellipse {
            device.connect(handler: self, secret: fleetKey)
            connection = .connecting
        } else if let device = ble.locks.first(where: {$0.macId == macId}) {
            ellipse = device
            connect()
        } else {
            connection = .search
            ble.scan(with: self)
        }
    }
    
    func disconnect() {
        ellipse?.disconnect()
    }
    
    func refreshStatus() {
        
    }
}

extension EllipseDeviceManager: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        if connected {
            connect()
        } else {
            buffer = security
            security = .undefined
            connection = .disconnected
        }
        send(.bleEnabled(connected))
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {
        if let device = ble.locks.first(where: {$0.macId == macId}), ellipse == nil {
            ellipse = device
            connect()
            lockManager.stopScan()
        }
    }
}


extension EllipseDeviceManager: EllipseDelegate {
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        switch security {
        case .locked:
            self.security = .locked
            if jammingHappend {
                jammingHappend = false
                send(.ellipseJamming(false))
            }
        case .unlocked:
            self.security = .unlocked
        case .invalid, .middle:
            self.security = .unlocked
            send(.ellipseJamming(true))
            jammingHappend = true
        default:
            return
        }
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        switch connection {
        case .unpaired:
            self.connection = .disconnected
            self.security = .undefined
        case .failed(let error):
            self.connection = .disconnected
            send(.failure(error))
        case .connecting:
            self.connection = .connecting
            self.security = .undefined
        case .paired, .ready:
            if let b = buffer {
                self.security = b
                buffer = nil
            }
            self.connection = .connected
            reconnectTimer?.invalidate()
            reconnectTimer = nil
        case .reconnecting:
            self.reconnect()
        default:
            break
        }
    }
    
    func reconnect() {
        connection = .search
        security = .undefined
        ble.scan()
        reconnectTimer = Timer.scheduledTimer(withTimeInterval: 30, repeats: false, block: { [weak self] timer in
            self?.connection = .disconnected
            self?.security = .undefined
            timer.invalidate()
            self?.ble.stopScan()
        })
    }
}
