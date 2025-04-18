//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 06.04.2022.
//

import Foundation
import Combine
import CoreBluetooth

public extension SasBLE {
    
    struct Device {
        
        public internal(set) var status: CurrentValueSubject<Status, Error> = .init(.disconnected)
        public internal(set) var isAround: CurrentValueSubject<Bool, Never> = .init(true)
        public let name: String
        public let id: String
        
        let connect: () -> Void
        let peripheral: CBPeripheral
        
        init(_ peripheral: CBPeripheral, advertisementData: [String : Any], connect: @escaping () -> Void) {
            self.peripheral = peripheral
            self.name = peripheral.name ?? "CAMLOCK"
            self.connect = connect
            let data = advertisementData["kCBAdvDataManufacturerData"] as? Data
            let string = data!.hexEncodedString()
            let start = string.index(string.startIndex, offsetBy: 6)
            let end = string.index(start, offsetBy: 10)
            self.id = String(string[start..<end])
        }
        
        public func unlock() {
            status.send(.connecting)
            connect()
        }
        
        func read(from characteristic: Characteristic) {
            guard let char = find(characteristic) else { return }
            peripheral.readValue(for: char)
        }
        
        func write(data: Data, to characteristic: Characteristic) {
            guard let char = find(characteristic) else { return }
            peripheral.writeValue(data, for: char, type: .withResponse)
        }
        
        func write(token: String) {
            guard let data = (token + "36").hexadecimal else { return }
            write(data: data, to: .unlock)
        }
        
        func didReceive(data: Data, from characteristic: Characteristic) -> String? {
            switch characteristic {
            case .nonce:
                let nonce = data.hexEncodedString()
                return nonce
            case .unlock:
                print(data)
            case .state:
                if data.hexEncodedString() == "0101" {
                    status.send(.unlocked)
                }
            }
            return nil
        }
        
        fileprivate func find(_ characteristic: Characteristic) -> CBCharacteristic? {
            let service = peripheral.services?.first(where: {$0.uuid == .sas})
            return service?.characteristics?.first(where: {$0.uuid == characteristic.uuid})
        }
    }
}

extension SasBLE.Device: Hashable, Identifiable {
    public static func == (lhs: SasBLE.Device, rhs: SasBLE.Device) -> Bool {
        lhs.id == rhs.id
    }
    
    public func hash(into hasher: inout Hasher) {
        hasher.combine(peripheral.hashValue)
    }
}

public extension SasBLE.Device {
    enum Status: String {
        case disconnected
        case connecting
        case connected
        case unlocking
        case unlocked
    }
}
