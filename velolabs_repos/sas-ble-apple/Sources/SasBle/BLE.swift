//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 14.02.2022.
//

import Foundation
import CoreBluetooth
import Combine

extension SAS {
    
    public final class BLE: NSObject {
        
        public fileprivate(set) var devices: Set<Device> = []
        public let found: PassthroughSubject<Device, Never> = .init()
        fileprivate var delayedScan: (() -> Void)?
        fileprivate let backend: SASBackend
        
        var central: CBCentralManager!
        
        public init(_ api: any SASBackend) {
            self.backend = api
            super.init()
            central = .init(delegate: self, queue: nil)
        }
        
        public func scan() {
            func perform() {
                central.scanForPeripherals(withServices: [.sas], options: [CBCentralManagerScanOptionAllowDuplicatesKey : false])
            }
            if central.state == .poweredOn {
                perform()
            } else {
                delayedScan = perform
            }
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
                    print(error)
                }
            }
        }
    }
}

extension SAS.BLE {
    public enum Characteristic: String, CaseIterable {
        case nonce = "a563020c-5d20-465f-b493-6a0031b9fcf3"
        case unlock = "a5630201-5d20-465f-b493-6a0031b9fcf3"
        case state = "a5630207-5d20-465f-b493-6a0031b9fcf3"
        
        var uuid: CBUUID {
            .init(string: rawValue)
        }
        
        static var all: [CBUUID] {
            Characteristic.allCases.map(\.uuid)
        }
    }
}

extension SAS.BLE: CBCentralManagerDelegate, CBPeripheralDelegate {
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn, let scan = delayedScan {
            scan()
            delayedScan = nil
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        guard !devices.contains(where: {$0.peripheral.identifier == peripheral.identifier}) else { return }
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

extension CBUUID {
    static var sas: CBUUID {
        .init(string: "a5630100-5d20-465f-b493-6a0031b9fcf3".uppercased())
    }
}

extension CBCharacteristic {
    var sas: SAS.BLE.Characteristic? {
        .init(rawValue: uuid.uuidString.lowercased())
    }
}
