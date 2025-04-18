//
//  KisiDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06.01.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Model


final class KisiDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .kisi
    
    var security: Device.Security = .undefined { didSet { sendState() }}
    
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    
    let consent: String? = "we_can_temporarily_turn_off".localized()
    
    let qrCode: String?
    
    let bleRestricted: Bool = false
    
    let thing: Thing
    let bike: Bike
    
    fileprivate let api: BikeAPI = AppRouter.shared.api()
    
    init(_ thing: Thing, bike: Bike) {
        self.thing = thing
        self.qrCode = thing.qrCode
        self.bike = bike
    }
    
    func lock() {
        guard security == .unlocked else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: { [weak self] in
            self?.security = .locked
        })
    }
    
    func unlock() {
        guard security == .locked else { return }
        security = .progress
        let keys = bike.controllers?.map(\.key) ?? []
        api.unlock(bikeId: bike.bikeId, controllers: keys) { [weak self] (result) in
            switch result {
            case .success:
                self?.security = .unlocked
                self?.lock()
            case .failure(let error):
                self?.security = .locked
                self?.send(.failure(error))
            }
        }
    }
    
    func connect() {
        guard !connection.in([.connected, .connecting]) else { return }
        connection = .connecting
        refreshStatus()
    }
    
    func disconnect() {
        connection = .disconnected
        security = .undefined
    }
    
    func refreshStatus() {
        api.iotStatus(bikeId: bike.bikeId, key: thing.key) { [weak self] (result) in
            switch result {
            case .failure(let error):
                self?.connection = .disconnected
                self?.send(.failure(error))
            case .success(let state):
                self?.connection = .connected
                self?.security = .locked//state.locked ? .locked : .unlocked
            }
        }
    }
    
}
