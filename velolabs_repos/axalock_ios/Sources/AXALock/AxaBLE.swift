


import Foundation
import CoreBluetooth

public struct AxaBLE {
    static let UUIDFormat = "0000%@-E513-11E5-9260-0002A5D5C51B"
    
    public enum Service: String {
        case lock = "1523"
        case invalid = "0000"
    }
    
    public enum Characteristic: String {
        case status = "1524"
        case control = "1525"
        case invalid = "0000"
    }
}

extension AxaBLE.Service: UUIDRepresentable, CaseIterable {}
extension AxaBLE.Characteristic: UUIDRepresentable, CaseIterable {}
extension CBUUID {
    var service: AxaBLE.Service {
        let raw = String(uuidString.prefix(8).suffix(4))
        return AxaBLE.Service(rawValue: raw) ?? .invalid
    }
    var characteristic: AxaBLE.Characteristic {
        let raw = String(uuidString.prefix(8).suffix(4))
        return AxaBLE.Characteristic(rawValue: raw) ?? .invalid
    }
}
extension CBCharacteristic {
    var axa: AxaBLE.Characteristic { uuid.characteristic }
}
extension CBService {
    var axa: AxaBLE.Service { uuid.service }
}

protocol UUIDRepresentable {
    var uuidString: String { get }
}

extension UUIDRepresentable where Self: RawRepresentable, Self.RawValue == String {
    var uuidString: String { .init(format: AxaBLE.UUIDFormat, rawValue) }
    var uuidValue: CBUUID { .init(string: uuidString) }
}

extension String {
    var bytesArray: [UInt8]? {
        if count % 2 == 1 {
            print("cannot convert \(self) to bytes string. Odd number of digits.")
            return nil
        }
        
        var bytes:[UInt8] = [UInt8]()
        var index:Int = 0
        while index < count {
            let startIndex = self.index(self.startIndex, offsetBy: index)
            let endIndex = self.index(startIndex, offsetBy: 2)
            let subString = self[startIndex..<endIndex]
            if let value = UInt8(subString, radix: 16) {
                bytes.append(value)
            }
            
            index += 2
        }
        
        return bytes
    }
}

extension CBUUID {
    static let batteryService = CBUUID(string: "0x180F")
    static let deviceInfoService = CBUUID(string: "0x180A")
    
    static let batteryLevel = CBUUID(string: "0x2A19")
    static let manufacturerName = CBUUID(string: "0x2A29")
    static let modelNumber = CBUUID(string: "0x2A24")
    static let serialNumber = CBUUID(string: "0x2A25")
    static let hardwareVersion = CBUUID(string: "0x2A27")
    static let firmwareVersion = CBUUID(string: "0x2A26")
    static let softwareVersion = CBUUID(string: "0x2A28")
    static let dfuService = CBUUID(string: "0xFE59")
    static let advertisementService = CBUUID(string: "8EC91523-F315-4F60-9FB8-838830DAEA50")
}

extension Data {
    var hex: String {
        return map { String(format: "%02x", $0) }.joined(separator: "")
    }
}

extension Array where Element == CBUUID {
    static var systemServices: [CBUUID] { [.batteryService, .deviceInfoService] }
    static var deviceInfoCharacterictics: [CBUUID] { [.manufacturerName, .modelNumber, .serialNumber, .hardwareVersion, .firmwareVersion, .softwareVersion]}
}
