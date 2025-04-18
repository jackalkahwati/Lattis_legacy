//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 06.04.2022.
//

import Foundation
import Combine
import CoreBluetooth

public final class SasBLE: NSObject {
    
    public fileprivate(set) var devices: Set<Device> = []
    public let found: PassthroughSubject<Device, Never> = .init()
    public let lost: PassthroughSubject<Device, Never> = .init()
    public let isOn: CurrentValueSubject<Bool, Never> = .init(false)
    fileprivate var delayedScan: (() -> Void)?
    fileprivate let backend: SASBackend
    fileprivate var availabilitySubscription: AnyCancellable?
    fileprivate var availability: Availability?
    
    var central: CBCentralManager!
    
    public init(_ api: any SASBackend) {
        self.backend = api
        super.init()
        central = .init(delegate: self, queue: nil)
    }
    
    public func scan(_ allowDuplicated: Bool = false) {
        guard !central.isScanning else { return }
        func perform() {
            central.scanForPeripherals(withServices: [.sas], options: [CBCentralManagerScanOptionAllowDuplicatesKey : allowDuplicated])
            if allowDuplicated {
                availability = .init(10)
                availabilitySubscription = availability?.report
                    .sink { [weak self] unavailible in
                        self?.report(unavailable: unavailible)
                    }
            }
        }
        if central.state == .poweredOn {
            perform()
        } else {
            delayedScan = perform
        }
    }
    
    public func stop() {
        guard central.isScanning else { return }
        central.stopScan()
        availabilitySubscription?.cancel()
        availability = nil
    }
    
    func find(_ peripheral: CBPeripheral) -> Device? {
        devices.first(where: {$0.peripheral.identifier == peripheral.identifier})
    }
    
    func getToken(for device: Device, with nonce: String) {
        Task {
            do {
                device.status.send(.unlocking)
                let token = try await backend.token(for: nonce, device: device.id)
                device.write(token: token)
            } catch {
                device.status.send(.disconnected)
                device.status.send(completion: .failure(error))
            }
        }
    }
    
    func report(unavailable: [UUID]) {
        let toRemove = devices.filter({unavailable.contains($0.peripheral.identifier)})
        toRemove.forEach { device in
            if let idx = devices.firstIndex(of: device) {
                devices.remove(at: idx)
            }
            lost.send(device)
        }
        
    }
}


extension SasBLE: CBCentralManagerDelegate, CBPeripheralDelegate {
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn, let scan = delayedScan {
            scan()
            delayedScan = nil
        }
        isOn.send(central.state == .poweredOn)
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        availability?.identifiers.insert(peripheral.identifier)
        guard !devices.contains(where: {$0.peripheral.identifier == peripheral.identifier}) else { return }
        availability?.checklist.insert(peripheral.identifier)
        let device = Device(peripheral, advertisementData: advertisementData) {
            central.connect(peripheral, options: nil)
        }
        let (fit, _) = devices.insert(device)
        if fit {
            found.send(device)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        guard let lock = devices.first(where: {$0.peripheral.identifier == peripheral.identifier}) else { return }
        lock.status.send(.disconnected)
    }
    
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self
        peripheral.discoverServices([.sas])
        if let device = find(peripheral) {
            device.status.send(.connected)
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        guard let service = peripheral.services?.first(where: {$0.uuid == .sas}) else { return }
        peripheral.discoverCharacteristics(Characteristic.all, for: service)
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        service.characteristics?.forEach({ characterictic in
            guard let char = characterictic.sas else { return }
            switch char {
            case .nonce:
                print(char)
                peripheral.readValue(for: characterictic)
            case .unlock:
                print(char)
            case .state:
                print(char)
                peripheral.setNotifyValue(true, for: characterictic)
            }
        })
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let device = find(peripheral), let char = characteristic.sas, let data = characteristic.value else { return }
        if let nonce = device.didReceive(data: data, from: char) {
            getToken(for: device, with: nonce)
        }
    }
}

