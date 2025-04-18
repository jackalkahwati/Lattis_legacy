//
//  SentinelDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06.04.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Model
import OvalBackend

final class SentinelDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .iot
    let consent: String? = "we_can_temporarily_turn_off".localized()
    var security: Device.Security = .undefined { didSet { sendState() }}
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    let qrCode: String?
    let bleRestricted: Bool = false
    
    let thing: Thing
    let bike: Bike
    
    fileprivate let backend: OvalBackend
    
    init(_ thing: Thing, bike: Bike) {
        self.thing = thing
        self.bike = bike
        self.qrCode = thing.qrCode
        self.backend = .init()
        
        NotificationCenter.default.addObserver(self, selector: #selector(notificationUnlocked), name: .sentinelOpen, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(notificationLocked), name: .sentinelClose, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(notificationOnline), name: .sentinelOnline, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    func lock() {
        guard security != .locked else { return }
        send(.axaCloseAlert(true))
    }
    
    @MainActor
    func unlock() {
        guard security != .unlocked else { return }
        security = .progress
        Task {
            do {
                let _ = try await self.backend.unlockSentinel(bikeId: bike.bikeId)
            } catch {
                if let e = error as? Failure, case "SentinelLockOffline" = e.name {
                    self.security = .unlocked
                    self.security = .locked
                    NotificationCenter.default.post(name: .sentinelOffline, object: nil)
                } else {
                    self.send(.failure(error))
                }
            }
        }
    }
    
    @MainActor
    func connect() {
        connection = .connecting
        refreshStatus()
    }
    
    func disconnect() {
        connection = .disconnected
    }
    
    @MainActor
    func refreshStatus() {
        Task {
            do {
                let status = try await self.backend.iotStatus(thing: thing)
                self.security = status.locked ? .locked : .unlocked
                self.connection = .connected
            } catch {
                self.send(.failure(error))
            }
        }
    }
    
    @objc
    func notificationUnlocked(_ notification: Notification) {
        security = .unlocked
    }
    
    @objc
    func notificationLocked(_ notification: Notification) {
        security = .locked
        send(.axaCloseAlert(false))
    }
    
    @objc
    func notificationOnline(_ notification: Notification) {
        DispatchQueue.main.async(execute: unlock)
    }
}

fileprivate extension OvalBackend {
    func unlockSentinel(bikeId: Int) async throws -> EmptyJSON {
        struct Device: Encodable {
            let deviceToken: String?
        }
        return try await post(Device(deviceToken: AppRouter.shared.notificationToken), endpoint: .init(.sentinel, path: "\(bikeId)/unlock"))
    }
    
    func iotStatus(thing: Thing) async throws -> Thing.Status {
        try await get(.init(.bikes, path: "\(thing.bikeId!)/iot/status", query: [.init(name: "controller_key", value: thing.key)]))
    }
}


