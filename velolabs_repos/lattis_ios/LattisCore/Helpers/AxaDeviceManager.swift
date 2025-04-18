//
//  AxaDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import AXALock
import EllipseLock

final class AxaDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .axa
    let consent: String? = nil
    var security: Device.Security = .undefined { didSet { sendState() }}
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    let qrCode: String? = nil
    var bleRestricted: Bool = false
    
    fileprivate var buffer: Device.Security?
    
    init(key: String) {
        self.key = key
        handler.bleStateUpdated = { [weak self] isOn in
            self?.bleRestricted = !isOn
            if isOn {
                self?.connect()
            } else {
                if let s = self?.security, s != .undefined {
                    self?.buffer = s
                }
                self?.security = .undefined
                self?.connection = .disconnected
            }
            self?.send(.bleEnabled(isOn))
        }
        handler.discovered = { [weak self] lock in
            if lock.id == key {
                self?.axa = lock
                self?.connect()
            }
        }
        handler.connectionChanged = { [weak self] lock in
            switch lock.connection {
            case .disconnected:
                self?.connection = .disconnected
                self?.security = .undefined
            case .paired:
                if let b = self?.buffer {
                    self?.security = b
                    self?.buffer = nil
                }
                self?.connection = .connected
                self?.security = lock.status.security
            case .security:
                self?.connection = .connecting
            default:
                break
            }
        }
        handler.statusChanged = { [weak self] lock in
            switch lock.status {
            case .open:
                self?.security = .unlocked
                self?.send(.axaCloseAlert(false))
            case .strongClosed:
                self?.security = .locked
                self?.send(.axaCloseAlert(false))
            case .weakClosed, .unsecuredOpen:
                self?.security = .progress
                self?.send(.axaCloseAlert(true))
            default:
                break
            }
        }
    }
    
    fileprivate let key: String
    fileprivate var axa: AxaBLE.Lock?
    fileprivate let handler = AxaBLE.Handler()
    
    func lock() {
        axa?.lock()
        security = .progress
    }
    
    func unlock() {
        axa?.unlock()
        security = .progress
    }
    
    func connect() {
        if let lock = axa {
            lock.connect(with: handler)
            if lock.connection == .paired {
                connection = .connected
                security = lock.status.security
            } else {
                connection = .connecting
            }
        } else if let lock = AxaBLE.Lock.all.first(where: {$0.id == key}) {
            axa = lock
            connect()
        } else {
            AxaBLE.Lock.scan(with: handler)
            connection = .search
        }
    }
    
    func disconnect() {
        axa?.disconnect()
    }
    
    func refreshStatus() {
        
    }
}

fileprivate extension AxaBLE.Lock.Status {
    var security: Device.Security {
        switch self {
        case .error, .unknown, .unsecuredOpen, .weakClosed:
            return .undefined
        case .strongClosed:
            return .locked
        case .open:
            return .unlocked
        }
    }
}
