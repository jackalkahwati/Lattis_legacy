//
//  AXAManager.swift
//  AXALock
//
//  Created by Ravil Khusainov on 10.02.2020.
//

import Foundation
import CoreBluetooth

public extension AxaBLE {
    class Manager: NSObject {
        var locks: [Lock] = []
        var handlersStore = NSHashTable<Handler>.weakObjects()
        var handlers: [Handler] { handlersStore.allObjects }
        var central: CBCentralManager!
        let cloud = AxaCloud()
        let servicesToScan: [CBUUID] = [Service.lock.uuidValue, .advertisementService]
        
        public override init() {
            super.init()
            central = CBCentralManager(delegate: self, queue: nil)
        }
        
        public func scan(with handler: Handler) {
            if !handlers.contains(handler) {
                handlersStore.add(handler)
            }
            startScan()
        }
        
        public func startScan() {
            guard central.state == .poweredOn else { return }
            central.scanForPeripherals(withServices: servicesToScan, options: [CBCentralManagerScanOptionAllowDuplicatesKey : false])
        }
        
        public func disconnect(lock: Lock) {
            central.cancelPeripheralConnection(lock.peripheral)
        }
        
        func add(handler: Handler) {
            guard !handlers.contains(handler) else { return }
            handlersStore.add(handler)
        }
        
        func connect(lock: Lock, with handler: Handler? = nil) {
            if let h = handler {
                add(handler: h)
            }
            guard lock.connection == .disconnected else {
                handlers.run(\.connectionChanged, with: lock)
                return
            }
            lock.delegate = self
            lock.peripheral.delegate = self
            cloud.requestKey(for: lock) { [weak self, weak lock] error in
                if let e = error, let l = lock {
                    self?.handlers.run(\.failed, with: l, error: e)
                    return
                }
                guard let l = lock else { return }
                self?.central.connect(l.peripheral, options: nil)
            }
        }
    }
}

extension AxaBLE.Manager: CBCentralManagerDelegate {
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        guard !handlers.isEmpty else { return }
        if central.state == .poweredOn {
            startScan()
        } else {
            locks.forEach{$0.connection = .disconnected}
        }
        handlers.forEach{$0.bleStateUpdated(central.state == .poweredOn)}
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        guard let lock = AxaBLE.Lock(peripheral), !locks.contains(lock) else { return }
        locks.append(lock)
        DispatchQueue.main.async {
            self.handlers.forEach({$0.discovered(lock)})
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        locks.first(where: \.peripheral, isEqual: peripheral)?.connection = .connected
        peripheral.discoverServices(servicesToScan + .systemServices)
    }
    
    public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        locks.first(where: \.peripheral, isEqual: peripheral)?.connection = .disconnected
    }
}

extension AxaBLE.Manager: CBPeripheralDelegate {
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        peripheral.services?.forEach({ (service) in
            if service.axa == .lock {
                peripheral.discoverCharacteristics(AxaBLE.Characteristic.allCases.map{$0.uuidValue}, for: service)
            }
            if service.uuid == CBUUID.batteryService {
                peripheral.discoverCharacteristics([.batteryLevel], for: service)
            }
            if service.uuid == CBUUID.deviceInfoService {
                peripheral.discoverCharacteristics(.deviceInfoCharacterictics, for: service)
            }
        })
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        service.characteristics?.forEach({ (characteristic) in
            if characteristic.axa == .status {
                peripheral.setNotifyValue(true, for: characteristic)
                peripheral.readValue(for: characteristic)
            }
            if characteristic.axa == .control {
                locks.first(where: \.peripheral, isEqual: peripheral)?.secure()
            }
            if [CBUUID].deviceInfoCharacterictics.contains(characteristic.uuid)
                || characteristic.uuid == CBUUID.batteryLevel {
                peripheral.readValue(for: characteristic)
            }
        })
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        locks.first(where: \.peripheral, isEqual: peripheral)?
            .onWrite(to: characteristic.axa, with: error)
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        locks.first(where: \.peripheral, isEqual: peripheral)?
            .onUpdate(value: characteristic.value, for: characteristic)
    }
}

public extension Sequence where Element == AxaBLE.Lock {
    func first<Value: Equatable>(where key: KeyPath<AxaBLE.Lock, Value>, isEqual to: Value) -> Element? {
        return first(where: {$0[keyPath: key] == to})
    }
}


extension Sequence where Element == AxaBLE.Handler {
    func run<Closure>(_ key: KeyPath<Element, Closure>, with lock: AxaBLE.Lock, error: Error? = nil) {
        forEach { (handler) in
            guard handler.locks.contains(lock) else { return }
            if let cl = handler[keyPath: key] as? (AxaBLE.Lock) -> () {
                cl(lock)
            }
            if let cl = handler[keyPath: key] as? (AxaBLE.Lock, Error) -> (), let e = error {
                cl(lock, e)
            }
            if let cl = handler[keyPath: key] as? (AxaBLE.Lock, AxaBLE.Lock.Cable) -> (), let c = lock.cable {
                cl(lock, c)
            }
        }
    }
}

extension AxaBLE.Manager: LockDelegate {
    func lock(_ lock: AxaBLE.Lock, cabkeStatusChanged to: AxaBLE.Lock.Cable) {
        DispatchQueue.main.async {
            self.handlers.run(\.cableStatusChanged, with: lock)
        }
    }
    
    func lock(_ lock: AxaBLE.Lock, connectionChanged from: AxaBLE.Lock.Connection) {
        DispatchQueue.main.async {
            self.handlers.run(\.connectionChanged, with: lock)
        }
    }
    
    func lock(_ lock: AxaBLE.Lock, statusChanged from: AxaBLE.Lock.Status) {
        DispatchQueue.main.async {
            self.handlers.run(\.statusChanged, with: lock)
        }
    }
    
    func lockNeedsNewKey(_ lock: AxaBLE.Lock) {
        cloud.requestKey(for: lock) { [weak self, weak lock] error in
            if let e = error, let l = lock {
                self?.handlers.run(\.failed, with: l, error: e)
                return
            }
            lock?.secure()
        }
    }
    
    func lock(_ lock: AxaBLE.Lock, didFailWith error: Error) {
        DispatchQueue.main.async {
            self.handlers.run(\.failed, with: lock, error: error)
        }
    }
    
    func lockDidUpdateInfo(_ lock: AxaBLE.Lock) {
        DispatchQueue.main.async {
            self.handlers.run(\.lockInfoUpdated, with: lock)
        }
    }
}

