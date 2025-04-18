//
//  Ellipse+Extensions.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/1/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import CoreBluetooth

public extension Ellipse {
    enum Service: String, UUIDCompatible {
        case security = "5E00"
        case hardware = "5E40"
        case configuration = "5E80"
        case test = "5EC0"
        case boot = "5D00"
        
        static let all: [CBUUID] = {
            let serv: [Service] = [.security, .hardware, .configuration, .test, .boot]
            return serv.map{$0.uuid}
        }()
        
        static let manage: [CBUUID] = {
            let serv: [Service] = [.hardware, .configuration, .test]
            return serv.map{$0.uuid}
        }()
        
        static let pairing: [CBUUID] = {
            return [Service.security.uuid]
        }()
    }
    
    enum Characteristic: String, UUIDCompatible {
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
        
        static let all: [Characteristic] = [
            .commandStatus, .LED, .lock, .hardwareInfo, .connection, .magnet, .accelerometer, .publicKey, .challengeKey, .challengeData, .signedMessage, .firmwareVersion, .writeFirmware, .writeFirmwareNotification, .firmwareUpdateDone, .resetLock, .serialNumber, .buttonSequece, .capTouch
        ]
        
        var shouldNotify: Bool {
            switch self {
            case .magnet, .accelerometer, .lock, .hardwareInfo, .writeFirmwareNotification:
                return true
            default:
                return false
            }
        }
    }
    
    enum Security: UInt8 {
        case unlocked = 0x00
        case locked = 0x01
        case middle = 0x04
        case invalid = 0x08
        case auto = 0xFF
    }
    
    enum Pin: UInt8 {
        case up = 0x01
        case right = 0x02
        case down = 0x04
        case left = 0x08
    }
    
    enum Byte: UInt8 {
        case null = 0x00
        case reset = 0xBB
        case factoryReset = 0xBC
        case securityOwnerVerified = 0x04
        case securityGuest = 0x01
        case securityOwner = 0x02
        case securityGuestVerified = 0x03
        case invalidLenghtWriteIgnored = 0x80
        case accessDenied = 0x81
        case lockUnlockFailed = 0x82
        case invalidOffcet = 0x83
        case invalidWriteLenght = 0x84
        case invalidParameter = 0x85
        case commandInProgress = 0xFF
    }
    
    enum Connection {
        case paired
        case unpaired
        case connecting
        case reconnecting
        case flashingLED
        case manageCapTouch
        case failed(Error)
        case updating(Float)
        case restored
        case ready
    }
    
    internal enum Status {
        case publicKey, challengeKey, challengeData, signedMessage, connected
    }
    
    internal enum Update {
        case none, inProgress, needReset
    }
    
    enum RestoringStrategy {
        case reconnect, disconnect
    }
    
    enum CachingStrategy {
        case `default`, never
    }
}

internal protocol UUIDCompatible {}
internal extension UUIDCompatible where Self: RawRepresentable, Self.RawValue == String {
    var uuid: CBUUID {
        return CBUUID(string: "D399\(self.rawValue)-FA57-11E4-AE59-0002A5D5C51B")
    }
    
    init?(_ uuid: CBUUID) {
        let string = uuid.uuidString
        let start = string.index(string.startIndex, offsetBy: 4)
        let end = string.index(start, offsetBy: 3)
        let str = string[start...end]
        self.init(rawValue: String(str))
    }
}

internal extension Ellipse.Characteristic {
    init?(_ characteristic: CBCharacteristic) {
        self.init(characteristic.uuid)
    }
}

internal extension Ellipse.Service {
    init?(_ service: CBService) {
        self.init(service.uuid)
    }
}

public extension Ellipse.Pin {
    var stringValue: String {
        return String(describing: self)
    }
}

extension CBPeripheral {
    var macId: String? {
        var result: String? = nil
        if let com = name?.components(separatedBy: " "), com.count == 2, let res = com.last {
            result = res
        } else if let com = name?.components(separatedBy: "-"), com.count == 2, let res = com.last {
            result = res
        }
        return result
    }
    
    var isEllboot: Bool {
        if let name = name {
            return name.contains("Ellboot")
        }
        return false
    }
}

extension CBPeripheralState {
    var string: String {
        switch self {
        case .connected:
            return "connected"
        case .connecting:
            return "connecting"
        case .disconnected:
            return "disconnected"
        case .disconnecting:
            return "disconnecting"
        @unknown default:
            return "unknown"
        }
    }
}
