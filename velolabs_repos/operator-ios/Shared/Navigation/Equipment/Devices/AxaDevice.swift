//
//  AxaDevice.swift
//  Operator (iOS)
//
//  Created by Ravil Khusainov on 28.10.2021.
//

import Foundation
import AXALock
import Combine

final class AxaDevice: PhysicalDevice {
    
    let thing: Thing
    let hwVersion: CurrentValueSubject<String?, Never> = .init(nil)
    let fwVersion: CurrentValueSubject<String?, Never> = .init(nil)
    let sfVersion: CurrentValueSubject<String?, Never> = .init(nil)
    let batteryLevel: CurrentValueSubject<Int?, Never> = .init(nil)
    let modelNumber: CurrentValueSubject<String?, Never> = .init(nil)
    
    init(_ thing: Thing) {
        self.thing = thing
        handler.discovered = { [unowned self] lock in
            guard lock.id == thing.metadata.key else { return }
            self.axa = lock
            self.link.send(.nearby)
        }
        handler.connectionChanged = { [unowned self] lock in
            switch lock.connection {
            case .disconnected:
                self.link.send(.disconnected)
            case .connected, .security:
                self.link.send(.connecting)
            case .paired:
                self.link.send(.connected)
            }
        }
        handler.cableStatusChanged = { [unowned self] (lock, cable) in
            
        }
        handler.statusChanged = { [unowned self] lock in
            self.handle(status: lock.status)
        }
        handler.lockInfoUpdated = { [unowned self] lock in
            self.handle(status: lock.status)
            self.batteryLevel.send(lock.batteryLevel)
            self.modelNumber.send(lock.modelNumber)
            self.sfVersion.send(lock.sfVersion)
            self.fwVersion.send(lock.fwVersion)
            self.hwVersion.send(lock.hwVersion)
        }
        handler.failed = { [unowned self] lock, error in
            print(error)
            self.link.send(completion: .failure(error))
        }
    }
    
    let security: CurrentValueSubject <Device.Security, Error> = .init(.undefined)
    let link: CurrentValueSubject <Device.Link, Error> = .init(.discovery)
    
    fileprivate let handler = AxaBLE.Handler()
    fileprivate var axa: AxaBLE.Lock?
    
    func connect() {
        axa?.connect(with: handler)
    }
    
    func disconnect() {
        axa?.disconnect()
    }
    
    func unlock() {
        axa?.unlock()
    }
    
    func lock() {
        axa?.lock()
    }
    
    func scan() {
        AxaBLE.Lock.scan(with: handler)
    }
    
    fileprivate func handle(status: AxaBLE.Lock.Status) {
        switch status {
        case .open:
            self.security.send(.unlocked)
        case .strongClosed:
            self.security.send(.locked)
        case .unknown, .error:
            self.security.send(.undefined)
        case .weakClosed, .unsecuredOpen:
            self.security.send(.locking)
        }
    }
}
