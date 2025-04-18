//
//  AXALock.swift
//  AXALock
//
//  Created by Ravil Khusainov on 10.02.2020.
//

import Foundation
import CoreBluetooth

protocol LockDelegate: AnyObject {
    func lock(_ lock: AxaBLE.Lock, connectionChanged from: AxaBLE.Lock.Connection)
    func lock(_ lock: AxaBLE.Lock, statusChanged from: AxaBLE.Lock.Status)
    func lock(_ lock: AxaBLE.Lock, cabkeStatusChanged to: AxaBLE.Lock.Cable)
    func lockNeedsNewKey(_ lock: AxaBLE.Lock)
    func lock(_ lock: AxaBLE.Lock, didFailWith error: Error)
    func lockDidUpdateInfo(_ lock: AxaBLE.Lock)
}

public extension AxaBLE {
    class Lock: Identifiable {
        public let id: String
        public fileprivate(set) var status: Status = .unknown {
            didSet {
                guard status != oldValue else { return }
                delegate?.lock(self, statusChanged: oldValue)
            }
        }
        public fileprivate(set) var cable: Cable? {
            didSet {
                guard let c = cable, c != oldValue else { return }
                delegate?.lock(self, cabkeStatusChanged: c)
            }
        }
        public internal(set) var connection: Connection = .disconnected {
            didSet {
                guard connection != oldValue else { return }
                if connection == .disconnected {
                    ekey.removeAll()
                    passkey.removeAll()
                }
                if connection == .security { delegate?.lockNeedsNewKey(self) }
                delegate?.lock(self, connectionChanged: oldValue)
            }
        }
        public var name: String { peripheral.name ?? "N/A" }
        public fileprivate(set) var manufacturerName: String?
        public fileprivate(set) var modelNumber: String?
        public fileprivate(set) var serialNumber: String?
        public fileprivate(set) var fwVersion: String?
        public fileprivate(set) var hwVersion: String?
        public fileprivate(set) var sfVersion: String?
        public fileprivate(set) var batteryLevel: Int?
        public var isMetadataComplete: Bool { manufacturerName != nil && modelNumber != nil && serialNumber != nil && fwVersion != nil && hwVersion != nil && sfVersion != nil }
        
        weak var delegate: LockDelegate?
        let peripheral: CBPeripheral
        var ekey: [String] = []
        var passkey: [String] = []
        
        public init?(_ peripheral: CBPeripheral) {
            self.peripheral = peripheral
            guard let name = peripheral.name, name.contains("AXA") else { return nil }
            id = name.replacingOccurrences(of: "AXA:", with: "")
        }
        
        public func connect(with handler: Handler? = nil) {
            handler?.add(self)
            AxaBLE.Lock.manager.connect(lock: self, with: handler)
        }
        
        public func disconnect() {
            AxaBLE.Lock.manager.disconnect(lock: self)
        }
        
        public func lock() {
            guard status == .open else { return }
            operate()
        }
        
        public func unlock() {
            guard status == .strongClosed else { return }
            operate()
        }
        
        fileprivate func operate() {
            guard connection != .security, !passkey.isEmpty else { return }
            let str = passkey.removeFirst()
            guard let bytes = str.bytesArray else { return }
            write(bytes: bytes, to: .control, of: .lock)
            if passkey.count == 1 {
                connection = .security
            }
        }
        
        func secure() {
            writeEkey()
        }
        
        func writeEkey() {
            guard !ekey.isEmpty, let bytes = ekey.removeFirst().bytesArray else {
                connection = .paired
                return
            }
            write(bytes: bytes, to: .control, of: .lock)
        }
        
        func write(bytes: [UInt8], to char: Characteristic, of service: Service) {
            guard let c = peripheral.characteristic(char, of: service) else { return }
            peripheral.writeValue(Data(bytes), for: c, type: .withResponse)
        }
        
        func read(from char: Characteristic, of service: Service) {
            guard let c = peripheral.characteristic(char, of: service) else { return }
            peripheral.readValue(for: c)
        }
        
        func onWrite(to char: Characteristic, with error: Error?) {
            if let err = error {
                delegate?.lock(self, didFailWith: err)
                print("Write finished with", err)
            } else {
                writeEkey()
            }
        }
        
        func onUpdate(value: Data?, for characteristic: CBCharacteristic) {
            guard let data = value else { return }
            let bytes = [UInt8](data)
            switch characteristic.axa {
            case .status:
                if var first = bytes.first {
                    if bytes.count == 2 {
                        first &= 0x0F
                        let c = first & 0xF0
                        if let cable = Cable(rawValue: c) {
                            self.cable = cable
                        }
                    }
                    if let status = Status(rawValue: first) {
                        self.status = status
                    }
                }
            default:
                break
            }
            guard characteristic.axa == .invalid else { return }
            var stringValue: String { String(data: data, encoding: .utf8) ?? "N/A"}
            switch characteristic.uuid {
            case .batteryLevel:
                if let first = bytes.first {
                    self.batteryLevel = Int(first)
                }
            case .manufacturerName:
                manufacturerName = stringValue
            case .firmwareVersion:
                fwVersion = stringValue
            case .hardwareVersion:
                hwVersion = stringValue
            case .modelNumber:
                modelNumber = stringValue
            case .softwareVersion:
                sfVersion = stringValue
            case .serialNumber:
                serialNumber = stringValue
            default:
                break
            }
            delegate?.lockDidUpdateInfo(self)
        }
    }
}

public extension AxaBLE.Lock {
    static let manager = AxaBLE.Manager()
    static var all: [AxaBLE.Lock] { manager.locks }
    static func scan(with handler: AxaBLE.Handler) { manager.scan(with: handler) }
    static func add(handler: AxaBLE.Handler) { manager.add(handler: handler) }
    static func claim(code: AxaCloud.Claim, compltion: @escaping (Result<Void, Error>) -> ()) {
        manager.cloud.claim(lock: code, completion: compltion)
    }
    
    enum Status: UInt8 {
        case unknown = 0x10
        case open = 0x00
        case unsecuredOpen = 0x08
        case weakClosed = 0x09
        case strongClosed = 0x01
        case error = 0xFF
    }
    
    enum Cable: UInt8 {
        case notInserted = 0x00
        case inserted = 0x80
        case locked = 0x10
    }
    
    enum Connection {
        case disconnected
        case connected
        case paired
        case security
    }
}

extension AxaBLE.Lock: Equatable {
    public static func == (lhs: AxaBLE.Lock, rhs: AxaBLE.Lock) -> Bool {
        lhs.id == rhs.id
    }
}

extension CBPeripheral {
    func characteristic(_ char: AxaBLE.Characteristic, of service: AxaBLE.Service) -> CBCharacteristic? {
        let serv = services?.first(where: {$0.axa == service})
        return serv?.characteristics?.first(where: {$0.axa == char})
    }
}

