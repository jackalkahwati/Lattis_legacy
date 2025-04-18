//
//  IoTDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30.09.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model
import Wrappers

final class IoTDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind = .iot
    let consent: String? = "we_can_temporarily_turn_off".localized()
    var security: Device.Security = .undefined { didSet { sendState() }}
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    let qrCode: String?
    let bleRestricted: Bool = false
    
    let bike: Bike
    let thing: Thing
    
    @UserDefaultsBacked(key: "thing.commandId")
    fileprivate var commandId: String?
    fileprivate let delay: TimeInterval = 5
    fileprivate var statusTimer: Timer?
    
    internal init(bike: Bike, thing: Thing) {
        self.bike = bike
        self.thing = thing
        self.qrCode = thing.qrCode
        commandId = nil
    }
    
    fileprivate let api: BikeAPI = AppRouter.shared.api()
    
    func lock() {
        guard security == .unlocked else { return }
        security = .progress
        let keys = bike.controllers?.map(\.key) ?? []
        api.lock(bikeId: bike.bikeId, controllers: keys) { [weak self] (result) in
            switch result {
            case .success(let cmd):
                if let command = cmd {
                    self?.send(.linkaOperationAlert(true, true))
                    self?.commandId = command
                    self?.runStatusCheck()
                } else {
                    self?.security = .locked
                }
            case .failure(let error):
                self?.security = .unlocked
                self?.send(.failure(error))
            }
        }
    }
    
    func unlock() {
        guard security == .locked else { return }
        security = .progress
        let keys = bike.controllers?.map(\.key) ?? []
        api.unlock(bikeId: bike.bikeId, controllers: keys) { [weak self] (result) in
            switch result {
            case .success(let cmd):
                if let command = cmd {
                    self?.send(.linkaOperationAlert(false, true))
                    self?.commandId = command
                    self?.runStatusCheck()
                } else {
                    self?.security = .unlocked
                }
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
    
    func refreshStatus() {
        api.iotStatus(bikeId: bike.bikeId, key: thing.key) { [weak self] (result) in
            switch result {
            case .failure(let error):
                self?.connection = .disconnected
                self?.send(.failure(error))
            case .success(let state):
                self?.connection = .connected
                self?.security = state.locked ? .locked : .unlocked
                if self?.commandId != nil {
                    self?.checkStatus()
                    self?.security = .progress
                }
            }
        }
    }
    
    func disconnect() {
        statusTimer?.invalidate()
        connection = .disconnected
        security = .undefined
    }
    
    fileprivate func runStatusCheck() {
        statusTimer?.invalidate()
        statusTimer = Timer.scheduledTimer(withTimeInterval: delay, repeats: true, block: { [weak self] _ in
            self?.checkStatus()
        })
    }
    
    fileprivate func checkStatus() {
        guard let command = commandId else {
            statusTimer?.invalidate()
            return
        }
        api.lockStatus(bikeId: bike.bikeId, commandId: command) { [weak self] result in
            switch result {
            case .failure(let error):
                self?.statusTimer?.invalidate()
                self?.security = .undefined
                self?.send(.failure(error))
            case .success(let feedback) where feedback.status == .finished:
                self?.send(.linkaOperationAlert(feedback.command == .lock, false))
                self?.statusTimer?.invalidate()
                if feedback.statusDesc == .lockSuccess {
                    self?.security = .locked
                } else if feedback.statusDesc == .unlockSuccess{
                    self?.security = .unlocked
                } else if feedback.command == .lock {
                    self?.security = .unlocked
                } else if feedback.command == .unlock {
                    self?.security = .locked
                }
            default:
                break
            }
        }
    }
}
