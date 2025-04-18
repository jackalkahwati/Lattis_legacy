//
//  EllipseDevice.swift
//  Operator (iOS)
//
//  Created by Ravil Khusainov on 11.10.2021.
//

import Foundation
import EllipseLock
import Combine

final class EllipseDevice: PhysicalDevice {
    
    let thing: Thing
    
    init(_ thing: Thing) {
        self.thing = thing
        fetchCredentials()
    }
    
    deinit {
        disconnect()
    }
    
    fileprivate(set) var isScanning: Bool = false
    let link: CurrentValueSubject <Device.Link, Error> = .init(.disconnected)
    fileprivate(set) var security: CurrentValueSubject <Device.Security, Error> = .init(.undefined)
    fileprivate(set) var credentials: CurrentValueSubject <Credentials?, Error> = .init(nil)
    fileprivate(set) var fwVersion: CurrentValueSubject <String?, Never> = .init(nil)
    fileprivate(set) var serialNumber: CurrentValueSubject <String?, Never> = .init(nil)
    fileprivate(set) var batteryLevel: CurrentValueSubject <String?, Never> = .init(nil)
    fileprivate(set) var capTouchEnabled: CurrentValueSubject <Bool, Never> = .init(true)
    fileprivate(set) var autoLockEnabled: CurrentValueSubject <Bool?, Never> = .init(nil)
    fileprivate(set) var shackleInserted: CurrentValueSubject <Bool?, Never> = .init(nil)
    
    fileprivate let manager = EllipseManager.shared
    fileprivate var ellipse: Ellipse?
    
    fileprivate var cancellabels: Set<AnyCancellable> = []
    
    fileprivate func fetchCredentials() {
        if let cred = OvalAPI.shared.credentials(for: thing.id) {
            credentials.send(cred)
        } else {
            CircleAPI.credentials(lockId: thing.id)
                .sink { [unowned self] result in
                    switch result {
                    case .failure(let error):
                        self.link.send(completion: .failure(error))
                    case .finished:
                        print("Credentials received for lock")
                    }
                } receiveValue: { [unowned self] credential in
                    OvalAPI.shared.save(credential: credential, for: self.thing.id)
                    self.credentials.send(credential)
                }
                .store(in: &cancellabels)
        }
    }
    
    func lock() {
        security.send(.locking)
        ellipse?.lock()
    }
    
    func unlock() {
        security.send(.unlocking)
        ellipse?.unlock()
    }
    
    func unlock(with pin: [Pin]) {
        ellipse?.unlock(with: pin.map(Ellipse.Pin.init))
    }
    
    func blink() {
        ellipse?.flashLED()
    }
    
    func setCapTouch(enabled: Bool) {
        ellipse?.isCapTouchEnabled = enabled
        capTouchEnabled.send(enabled)
    }
    
    func setAutoLock(enabled: Bool) {
        ellipse?.isMagnetAutoLockEnabled = enabled
        autoLockEnabled.send(enabled)
    }
    
    func reset() {
        ellipse?.factoryReset(disconnect: false)
    }
    
    func connect() {
        ellipse?.connect(handler: self, secret: credentials.value?.secret)
    }
    
    func disconnect() {
        ellipse?.disconnect()
    }
    
    func scan() {
        isScanning = true
        manager.scan(with: self)
    }
    
    func stopScan() {
        isScanning = false
        manager.stopScan()
    }
    
    fileprivate func handle(_ security: Ellipse.Security) {
        switch security {
        case .locked:
            self.security.send(.locked)
        case .unlocked:
            self.security.send(.unlocked)
        default:
            break
        }
    }
}

extension EllipseDevice: EllipseManagerDelegate, EllipseDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {
        if ellipse == nil, let found = lockManager.locks.first(where: {$0.macId == credentials.value?.macId}) {
            ellipse = found
            link.send(.nearby)
        }
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        handle(security)
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        switch connection {
        case .paired:
            link.send(.connected)
        case .ready:
            handle(ellipse.security)
        case .connecting, .reconnecting:
            link.send(.connecting)
        case .unpaired:
            link.send(.disconnected)
        case .failed(let error):
            link.send(completion: .failure(error))
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate value: Ellipse.Value) {
        switch value {
        case .firmwareVersion(let ver):
            fwVersion.send(ver)
        case .metadata(let meta):
            let level = Int(meta.batteryLevel*100)
            batteryLevel.send("\(level)%")
        case .capTouchEnabled(let enabled):
            capTouchEnabled.send(ellipse.isCapTouchEnabled ?? enabled)
        case .serialNumber(let number):
            serialNumber.send(number)
        case .magnetAutoLockEnabled(let enabled):
            autoLockEnabled.send(ellipse.isMagnetAutoLockEnabled ?? enabled)
        case .shackleInserted:
            shackleInserted.send(ellipse.isShackleInserted)
        default:
            break
        }
    }
    
}


extension EllipseManager {
    static var shared: EllipseManager {
        if uninitialized.api == nil {
            uninitialized.api = OvalAPI.shared
            uninitialized.restoringStrategy = .disconnect
        }
        return uninitialized
    }
}

extension Ellipse.Pin {
    init(_ code: EllipseDevice.Pin) {
        switch code {
        case .up:
            self = .up
        case .down:
            self = .down
        case .left:
            self = .left
        case .right:
            self = .right
        }
    }
}
