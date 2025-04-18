

import Foundation
import CoreBluetooth

fileprivate let connectionTime: TimeInterval = 20
fileprivate let writeLimit: Int = 132 // Maximum bytes count allowed to write at the time
fileprivate let magnetShackleDetected: Float = 8192

protocol EllipseLockDelegate: class {
    func lockDidUpdateConnection(_ lock: EllipseLock)
    func lockDidUpdateSecurity(_ lock: EllipseLock)
    func lock(_ lock: EllipseLock, didFailWith error: Error)
    func lock(_ lock: EllipseLock, didUpdate metadata: EllipseBLE.Metadata)
    func lock(_ lock: EllipseLock, didUpdate magnet: Accelerometer.Coordinate)
}

public final class EllipseLock: EllipseProtocol {
    
    public typealias Handler = EllipseHandler<EllipseLock>
    
    public let macId: String
    public let name: String
    public let isFactoryMode: Bool
    public internal(set) var serialNumber: String = "0"
    public internal(set) var fwVersion: String = "0"
    public internal(set) var bootLoaderVersion: String = "0"
    public internal(set) var connection: EllipseBLE.Connection = .disconnected {
        didSet {
            guard connection != oldValue else { return }
            delegate?.lockDidUpdateConnection(self)
        }
    }
    public fileprivate(set) var security: EllipseBLE.SecurityValue = .middle {
        didSet {
            guard security != oldValue else { return }
            delegate?.lockDidUpdateSecurity(self)
        }
    }
    public fileprivate(set) var metadata: EllipseBLE.Metadata?
    public fileprivate(set) var magnet: Accelerometer.Coordinate = .zero
    public var shackle: Bool = false
    
    let peripheral: CBPeripheral
    weak var delegate: EllipseLockDelegate?
    
    init?(_ peripheral: CBPeripheral) {
        self.peripheral = peripheral
        guard let name = peripheral.name else { return nil }
        self.name = name
        if name.contains("-") { // Ellipse-MAC_ID
            let com = name.components(separatedBy: "-")
            guard let res = com.last else { return nil }
            self.macId = res
            self.isFactoryMode = true
        } else {
            let com = name.components(separatedBy: " ")
            guard let res = com.last else { return nil }
            self.macId = res
            self.isFactoryMode = false
        }
    }
    
    public func connect(with handler: Handler? = nil) {
        handler?.add(self)
        EllipseLock.manager.connect(lock: self, with: handler)
    }
    
    public func disconnect() {
        EllipseLock.manager.disconnect(lock: self)
    }
    
    func handle(value: [UInt8], for characteristic: EllipseBLE.Characteristic) {
        switch characteristic {
        case .commandStatus:
            didUpdateCommandStatus(value: value)
        case .lock:
            didUpdateSecurity(value: value)
        case .hardwareInfo:
            didUpdateMetadata(value: value)
        case .magnet:
            didUpdateMagnet(data: value)
        case .firmwareVersion:
            if let fw = Data(value).ellipseVersion {
                fwVersion = fw
            }
            if let bl = Data(value).bootLoaderVersion {
                bootLoaderVersion = bl
            }
        case .serialNumber:
            if let serial = String(data: Data(value), encoding: .utf8) {
                serialNumber = serial
            }
        default:
            break
        }
    }
    
    fileprivate func didUpdateCommandStatus(value: [UInt8]) {
        guard let byte: EllipseBLE.CommandValue = value.first() else { return }
        switch byte {
        case .lockUnlockFailed:
            security = .middle
        default:
            break
        }
    }
    
    fileprivate func didUpdateSecurity(value: [UInt8]) {
        guard let first: EllipseBLE.SecurityValue = value.first() else { return }
        security = first
    }
    
    fileprivate func didUpdateMetadata(value: [UInt8]) {
        guard var metadata = EllipseBLE.Metadata(bytes: value) else { return }
        
        // Hack to prevent reading voltage while lock/unlock beeing perfomed
        if metadata.security != security, let voltage = self.metadata?.voltage {
            metadata.voltage = voltage
        }
        self.metadata = metadata
        security = metadata.security
        delegate?.lock(self, didUpdate: metadata)
    }
    
    fileprivate func didUpdateMagnet(data: [UInt8]) {
        guard data.count >= 6 else { return }
        magnet = Accelerometer.Coordinate(bytes: data, x: 0, y: 2, z: 4, count: 2)
        shackle = abs(magnet.z) >= magnetShackleDetected
        delegate?.lock(self, didUpdate: magnet)
    }
    
    fileprivate func write<Value: RawRepresentable>(value: Value, to char: EllipseBLE.Characteristic, of service: EllipseBLE.Service) where Value.RawValue == UInt8 {
        write(value: [value.rawValue], to: char, of: service)
    }
    
    fileprivate func write(value: [UInt8], to char: EllipseBLE.Characteristic, of service: EllipseBLE.Service) {
        do {
            let characteristic = try peripheral.characteristic(char, of: service)
            peripheral.writeValue(Data(value), for: characteristic, type: .withResponse)
        } catch {
            delegate?.lock(self, didFailWith: error)
        }
    }
}

extension CBPeripheral {
    func characteristic(_ char: EllipseBLE.Characteristic, of service: EllipseBLE.Service) throws -> CBCharacteristic {
        guard let serv = services?.first(where: {$0.ellipse == service}) else { throw EllipseError.missingService(service) }
        guard let c = serv.characteristics?.first(where: {$0.ellipse == char}) else { throw EllipseError.missingChar(char) }
        return c
    }
}

extension EllipseLock {
    static let manager = EllipseManager()
    public static var all: [EllipseLock] { manager.locks }
    
    public static func scan(with handler: EllipseHandler<EllipseLock>) { manager.scan(with: handler) }
    public static func add(handler: EllipseHandler<EllipseLock>) { manager.add(handler: handler) }
}

extension Array where Element == UInt8 {
    func first<Value: RawRepresentable>() -> Value? where Value.RawValue == UInt8 {
        guard let f = first else { return nil }
        return Value(rawValue: f)
    }
}
