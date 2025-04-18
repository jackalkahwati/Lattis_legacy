//
//  SasEquipment.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 2022-05-14.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Combine
import SasBLE
import Model
import OvalBackend

final class SasEquipment: Equipment {
    
    let thing: Thing
    var callback: PassthroughSubject<EquipmentControler.Callback, Error> = .init()
    
    fileprivate var device: SasBLE.Device?
    fileprivate var manager: SasBLE
    fileprivate var stateStorage: AnyCancellable?
    fileprivate var genericStorage: Set<AnyCancellable> = []
    fileprivate let warningMessage: String = "bluetooth_access_alert_message".localized()
    
    init(_ thing: Thing) {
        self.thing = thing
        self.manager = .init(OvalBackend(fleetId: thing.fleetId))
        subscribe()
        connect()
    }
    
    func lock() {
        callback.send(.status(.locked))
    }
    
    func unlock() {
        callback.send(.status(.processing))
        device?.unlock()
    }
    
    func connect() {
        manager.scan(true)
    }
    
    fileprivate func subscribe() {
        manager.isOn
            .sink { [unowned self] isOn in
                if isOn {
                    self.connect()
                    self.callback.send(.hide(message: self.warningMessage))
                } else {
                    self.callback.send(.status(.disconnected))
                    self.device = nil
                    self.callback.send(.show(message: self.warningMessage))
                }
            }
            .store(in: &genericStorage)
        manager.found
            .sink { [unowned self] device in
                guard device.id.lowercased() == self.thing.key.lowercased() && self.device == nil else { return }
                self.connected(device: device)
            }
            .store(in: &genericStorage)
        manager.lost
            .sink { [unowned self] device in
                guard device.id.lowercased() == self.thing.key.lowercased() && self.device != nil else { return }
                self.device = nil
                self.callback.send(.status(.disconnected))
            }
            .store(in: &genericStorage)
    }
    
    fileprivate func connected(device: SasBLE.Device) {
        self.device = device
        lock()
        stateStorage?.cancel()
        stateStorage = device.status
            .sink { [unowned self] completion in
                switch completion {
                case .failure(let error):
                    self.callback.send(completion: .failure(error))
                default:
                    break
                }
            } receiveValue: { [unowned self] status in
                switch status {
                case .unlocked:
                    self.callback.send(.status(.unlocked))
                    self.lock()
                    self.callback.send(.alert(.success))
                default:
                    break
                }
            }
    }
}
