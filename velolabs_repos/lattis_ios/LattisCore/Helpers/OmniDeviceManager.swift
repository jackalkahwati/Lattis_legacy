//
//  OmniDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 20.08.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import Model
import Wrappers

final class OmniDeviceManager: DeviceRepresenting {
    
    let kind: Device.Kind
    let consent: String? = "we_can_temporarily_turn_off".localized()
    var security: Device.Security = .undefined { didSet { sendState() }}
    var connection: Device.Connection = .disconnected { didSet { sendState() }}
    let bleRestricted: Bool = false
    var qrCode: String?
    
    let controllers: [String]
    let thing: Thing
    
    @UserDefaultsBacked(key: "thing.commandId")
    fileprivate var commandId: String?
    fileprivate let delay: TimeInterval = 10
    fileprivate var statusTimer: Timer?
    
    internal init(_ thing: Thing, controllers: [String]) {
        self.controllers = controllers
        self.thing = thing
        self.kind = .omni
        self.qrCode = thing.qrCode
        commandId = nil
    }
    
    fileprivate let api: BikeAPI = AppRouter.shared.api()
    
    func lock() {
        guard security == .unlocked, let bikeId = thing.bikeId else { return }
        security = .progress
        api.iotStatus(bikeId: bikeId, key: thing.key) { [weak self] result in
            switch result {
            case .success(let status):
                self?.security = status.locked ? .locked : .unlocked
                if !status.locked {
                    self?.showLockAlert()
                }
            case .failure(let error):
                self?.security = .unlocked
                self?.send(.failure(error))
            }
        }
    }
    
    func unlock() {
        guard security == .locked, let bikeId = thing.bikeId else { return }
        security = .progress
        api.unlock(bikeId: bikeId, controllers: controllers) { [weak self] (result) in
            switch result {
            case .success(let cmd):
                if let command = cmd {
                    self?.send(.linkaOperationAlert(false, true))
                    self?.commandId = command
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
        guard !connection.in([.connected, .connecting]), let bikeId = thing.bikeId else { return }
        connection = .connecting
        api.iotStatus(bikeId: bikeId, key: thing.key) { [weak self] (result) in
            switch result {
            case .failure(let error):
                self?.connection = .disconnected
                self?.send(.failure(error))
            case .success(let state):
                self?.connection = .connected
                self?.security = state.locked ? .locked : .unlocked
                if self?.commandId != nil {
//                    self?.checkStatus()
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
    
    func refreshStatus() {
        
    }
    
    func didSet(security: Device.Security) {
        self.security = security
    }
    
    fileprivate func showLockAlert() {
        send(.axaCloseAlert(true))
        statusTimer?.invalidate()
        statusTimer = Timer.scheduledTimer(withTimeInterval: delay, repeats: false, block: { [weak self] timer in
            timer.invalidate()
            self?.security = .unlocked
            self?.send(.axaCloseAlert(false))
        })
    }
//    fileprivate func runStatusCheck() {
//        statusTimer?.invalidate()
//        statusTimer = Timer.scheduledTimer(withTimeInterval: delay, repeats: true, block: { [weak self] _ in
//            self?.checkStatus()
//        })
//    }
//
//    fileprivate func checkStatus() {
//        guard let command = commandId else {
//            statusTimer?.invalidate()
//            return
//        }
//        api.lockStatus(bikeId: bikeId, commandId: command) { [weak self] result in
//            switch result {
//            case .failure(let error):
//                self?.statusTimer?.invalidate()
//                self?.security = .undefined
//                self?.send(.failure(error))
//            case .success(let feedback) where feedback.status == .finished:
//                self?.send(.linkaOperationAlert(feedback.command == .lock, false))
//                self?.statusTimer?.invalidate()
//                if feedback.statusDesc == .lockSuccess {
//                    self?.security = .locked
//                } else if feedback.statusDesc == .unlockSuccess{
//                    self?.security = .unlocked
//                } else if feedback.command == .lock {
//                    self?.security = .unlocked
//                } else if feedback.command == .unlock {
//                    self?.security = .locked
//                }
//            default:
//                break
//            }
//        }
//    }
}

