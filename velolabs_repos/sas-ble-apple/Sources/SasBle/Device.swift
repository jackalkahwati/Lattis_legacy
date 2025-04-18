//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 14.02.2022.
//

import Foundation
import CoreBluetooth
import Combine
import CryptoKit

extension Data {
    func hexEncodedString() -> String {
        return map { String(format: "%02hhx", $0) }.joined()
    }
}

extension String {
    
    /// Create `Data` from hexadecimal string representation
    ///
    /// This creates a `Data` object from hex string. Note, if the string has any spaces or non-hex characters (e.g. starts with '<' and with a '>'), those are ignored and only hex characters are processed.
    ///
    /// - returns: Data represented by this hexadecimal string.
    
    var hexadecimal: Data? {
        var data = Data(capacity: count / 2)
        
        let regex = try! NSRegularExpression(pattern: "[0-9a-f]{1,2}", options: .caseInsensitive)
        regex.enumerateMatches(in: self, range: NSRange(startIndex..., in: self)) { match, _, _ in
            let byteString = (self as NSString).substring(with: match!.range)
            let num = UInt8(byteString, radix: 16)!
            data.append(num)
        }
        
        guard data.count > 0 else { return nil }
        
        return data
    }
    
}

extension SAS.BLE {
    public struct Device {
        
        public internal(set) var status: CurrentValueSubject<Status, Never> = .init(.disconnected)
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
            print(id)
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
                print("nounce", nonce)
                return nonce
            case .unlock:
                print(data)
            case .state:
                print("state", data.count)
            }
            return nil
        }
        
        fileprivate func find(_ characteristic: Characteristic) -> CBCharacteristic? {
            let service = peripheral.services?.first(where: {$0.uuid == .sas})
            return service?.characteristics?.first(where: {$0.uuid == characteristic.uuid})
        }
    }
}

extension SAS.BLE.Device: Hashable, Identifiable {
    public static func == (lhs: SAS.BLE.Device, rhs: SAS.BLE.Device) -> Bool {
        lhs.id == rhs.id
    }
    
    public func hash(into hasher: inout Hasher) {
        hasher.combine(peripheral.hashValue)
    }
}

public extension SAS.BLE.Device {
    enum Status {
        case disconnected
        case connecting
        case connected
        case unlocking
        case unlocked
    }
}

