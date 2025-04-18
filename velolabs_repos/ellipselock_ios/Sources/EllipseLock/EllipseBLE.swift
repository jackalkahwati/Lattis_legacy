//
//  EllipseBLE.swift
//  EllipseLock
//
//  Created by Ravil Khusainov on 28.02.2020.
//

import Foundation
import CoreBluetooth

public struct EllipseBLE {
    public static let UUIDFormat = "D399%@-FA57-11E4-AE59-0002A5D5C51B"
    
    public enum Service: String {
        case security = "5E00"
        case hardware = "5E40"
        case configuration = "5E80"
        case test = "5EC0"
        case boot = "5D00"
        case invalid = "0000"
    }
    
    public enum Characteristic: String {
        case LED = "5E41"
        case lock = "5E42"
        case hardwareInfo = "5E43"
        case connection = "5E45"
        case magnet = "5E44"
        case accelerometer = "5E46"
        case signedMessage = "5E01"
        case publicKey = "5E02"
        case challengeKey = "5E03"
        case challengeData = "5E04"
        case firmwareVersion = "5D01"
        case writeFirmware = "5D02"
        case writeFirmwareNotification = "5D03"
        case firmwareUpdateDone = "5D04"
        case resetLock = "5E81"
        case serialNumber = "5E83"
        case capTouch = "5E82"
        case buttonSequece = "5E84"
        case commandStatus = "5E05"
        case invalid = "0000"
    }
    
    public enum Connection {
        case disconnected
        case connected
        case paired
        case reconnecting
        case fwUpdate
    }
}

extension EllipseBLE.Characteristic {
    var shouldNotify: Bool {
        switch self {
        case .magnet, .accelerometer, .lock, .hardwareInfo, .writeFirmwareNotification, .commandStatus:
            return true
        default:
            return false
        }
    }
}

extension EllipseBLE.Service: UUIDRepresentable, CaseIterable {}
extension EllipseBLE.Characteristic: UUIDRepresentable, CaseIterable {}
extension CBUUID {
    var service: EllipseBLE.Service {
        let raw = String(uuidString.prefix(8).suffix(4))
        return EllipseBLE.Service(rawValue: raw) ?? .invalid
    }
    var characteristic: EllipseBLE.Characteristic {
        let raw = String(uuidString.prefix(8).suffix(4))
        return EllipseBLE.Characteristic(rawValue: raw) ?? .invalid
    }
}
extension CBCharacteristic {
    var ellipse: EllipseBLE.Characteristic { uuid.characteristic }
}
extension CBService {
    var ellipse: EllipseBLE.Service { uuid.service }
}

protocol UUIDRepresentable {
    var uuidString: String { get }
}

extension UUIDRepresentable where Self: RawRepresentable, Self.RawValue == String {
    var uuidString: String { .init(format: EllipseBLE.UUIDFormat, rawValue) }
    var uuidValue: CBUUID { .init(string: uuidString) }
}
