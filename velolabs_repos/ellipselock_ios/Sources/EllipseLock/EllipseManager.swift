//
//  EllipseManager.swift
//  EllipseLock
//
//  Created by Ravil Khusainov on 28.02.2020.
//

import Foundation
import CoreBluetooth

class EllipseManager: NSObject {
    typealias Handler = EllipseHandler<EllipseLock>
    fileprivate let servicesToScan: [CBUUID] = EllipseBLE.Service.allCases.map{$0.uuidValue}
    fileprivate let charsToDiscover: [CBUUID] = EllipseBLE.Characteristic.allCases.map{$0.uuidValue}
    fileprivate(set) var locks: [EllipseLock] = []
    fileprivate var handlersStore = NSHashTable<Handler>.weakObjects()
    fileprivate var handlers: [Handler] { handlersStore.allObjects }
    fileprivate var central: CBCentralManager!
    
    override init() {
        super.init()

        central = CBCentralManager(delegate: self, queue: nil)
    }
        
    func scan(with handler: Handler) {
        add(handler: handler)
        startScan()
    }
    
    func startScan() {
        guard central.state == .poweredOn, !central.isScanning else { return }
        central.scanForPeripherals(withServices: servicesToScan, options: [CBCentralManagerScanOptionAllowDuplicatesKey : false])
    }
    
    func stopScan() {
        guard central.isScanning else { return }
        central.stopScan()
    }
    
    func add(handler: Handler) {
        guard !handlers.contains(handler) else { return }
        handlersStore.add(handler)
    }
    
    func connect(lock: EllipseLock, with handler: Handler?) {
        if let h = handler {
            add(handler: h)
        }
        lock.delegate = self
        lock.peripheral.delegate = self
        central.connect(lock.peripheral, options: nil)
    }
    
    func disconnect(lock: EllipseLock) {
        central.cancelPeripheralConnection(lock.peripheral)
    }
}

extension EllipseManager: CBCentralManagerDelegate {
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        guard !handlers.isEmpty else { return }
        startScan()
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        guard let lock = EllipseLock(peripheral), !locks.contains(lock) else { return }
        locks.append(lock)
        DispatchQueue.main.async {
            self.handlers.forEach{$0.discovered(lock)}
        }
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        locks.first(where: \.peripheral, isEqual: peripheral)?.connection = .connected
        peripheral.discoverServices(servicesToScan)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        locks.first(where: \.peripheral, isEqual: peripheral)?.connection = .disconnected
    }
    
    fileprivate func check(_ e: Error?, peripheral: CBPeripheral) -> Bool {
        if let error = e, let lock = locks.first(where: \.peripheral, isEqual: peripheral) {
            handlers.run(\.failed, with: lock, error: error)
            return false
        }
        return true
    }
}

extension EllipseManager: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        guard check(error, peripheral: peripheral) else { return }
        peripheral.services?.forEach({ (service) in
            peripheral.discoverCharacteristics(charsToDiscover, for: service)
        })
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        guard check(error, peripheral: peripheral) else { return }
        service.characteristics?.forEach({ (char) in
            if char.ellipse.shouldNotify {
                peripheral.setNotifyValue(true, for: char)
            }
            if let data = char.value, char.ellipse != .commandStatus {
                self.locks.first(where: \.peripheral, isEqual: peripheral)?.handle(value: [UInt8](data), for: char.ellipse)
            }
        })
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard check(error, peripheral: peripheral), let data = characteristic.value else { return }
        locks.first(where: \.peripheral, isEqual: peripheral)?.handle(value: [UInt8](data), for: characteristic.ellipse)
    }
}

extension EllipseManager: EllipseLockDelegate {
    func lockDidUpdateConnection(_ lock: EllipseLock) {
        DispatchQueue.main.async {
            self.handlers.run(\.connectionUpdated, with: lock)
        }
    }
    
    func lockDidUpdateSecurity(_ lock: EllipseLock) {
        DispatchQueue.main.async {
            self.handlers.run(\.securityUpdated, with: lock)
        }
    }
    
    func lock(_ lock: EllipseLock, didUpdate metadata: EllipseBLE.Metadata) {
        DispatchQueue.main.async {
            self.handlers.run(\.metadataUpdated, with: lock)
        }
    }
    
    func lock(_ lock: EllipseLock, didFailWith error: Error) {
        DispatchQueue.main.async {
            self.handlers.run(\.failed, with: lock, error: error)
        }
    }
    
    func lock(_ lock: EllipseLock, didUpdate magnet: Accelerometer.Coordinate) {
        DispatchQueue.main.async {
            self.handlers.run(\.magnetDataUpdated, with: lock)
        }
    }
}

public extension Sequence where Element == EllipseLock {
    func first<Value: Equatable>(where key: KeyPath<EllipseLock, Value>, isEqual to: Value) -> Element? {
        return first(where: {$0[keyPath: key] == to})
    }
}


public extension Sequence {
    func run<Lock: EllipseProtocol>(_ key: KeyPath<Element, (Lock) -> ()>, with lock: Lock) where Element == EllipseHandler<Lock> {
        forEach { (handler) in
            guard handler.locks.contains(lock) else { return }
            handler[keyPath: key](lock)
        }
    }
    
    func run<Lock: EllipseProtocol>(_ key: KeyPath<Element, (Lock, Error) -> ()>, with lock: Lock, error: Error) where Element == EllipseHandler<Lock> {
        forEach { (handler) in
            guard handler.locks.contains(lock) else { return }
            handler[keyPath: key](lock, error)
        }
    }
}
