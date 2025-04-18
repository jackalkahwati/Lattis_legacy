//
//  Ellipse+Extensions.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/12/2016.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation
import CommonCrypto
import CoreBluetooth

extension Array {
    func find(_ filter:(Element) -> Bool) -> Element? {
        for item in self where filter(item) {
            return item
        }
        return nil
    }
}

public extension String {
    internal var macId: String? {
        let hypenParts = self.components(separatedBy: "-")
        if hypenParts.count == 2 {
            return hypenParts[1]
        }
        
        let spacedParts = self.components(separatedBy: " ")
        if spacedParts.count == 2 {
            return spacedParts[1]
        }
        
        return nil
    }
    
    public var dataValue: Data? {
        guard let bytes = bytesArray else { return nil }
        return Data(bytes: bytes)
    }
    
    public var bytesArray: [UInt8]? {
        let count = self.characters.count
        if count % 2 == 1 {
            print("cannot convert \(self) to bytes string. Odd number of digits.")
            return nil
        }
        
        var bytes:[UInt8] = [UInt8]()
        var index:Int = 0
        while index < count {
            let startIndex = self.index(self.startIndex, offsetBy: index)
            let endIndex = self.index(startIndex, offsetBy: 2)
            let subString = self.substring(with: startIndex..<endIndex)
            if let value = UInt8(subString, radix: 16) {
                bytes.append(value)
            }
            
            index += 2
        }
        
        return bytes
    }
    
    var md5: String {
        let str = self.cString(using: String.Encoding.utf8)
        let strLen = CC_LONG(self.lengthOfBytes(using: String.Encoding.utf8))
        let digestLen = Int(CC_MD5_DIGEST_LENGTH)
        let result = UnsafeMutablePointer<CUnsignedChar>.allocate(capacity: digestLen)
        
        CC_MD5(str!, strLen, result)
        
        let hash = NSMutableString()
        for i in 0..<digestLen {
            hash.appendFormat("%02x", result[i])
        }
        
        result.deallocate(capacity: digestLen)
        
        return String(format: hash as String)
    }
}

public extension Data {
    public var ellipseVersion: String? {
        let versionIndex = 9
        let reversionIndex = 11
        
        var values = [UInt8](self)
        
        guard values.count > versionIndex else { return nil }
        var version = "\(values[versionIndex])"
        
        guard values.count > reversionIndex else { return version }
        version += String(format: ".%02d", values[reversionIndex])
        
        return version
    }
    
    var sha256: Data {
        var hash = [UInt8](repeating: 0,  count: Int(CC_SHA256_DIGEST_LENGTH))
        self.withUnsafeBytes {
            _ = CC_SHA256($0, CC_LONG(self.count), &hash)
        }
        return Data(bytes: hash)
    }
}

public extension Peripheral {
    public func readFirmwareVersion(completion: @escaping (String?) -> ()) {
        do {
            try read(.firmwareVersion, completion: { (data) in
                guard let data = data else { return completion(nil) }
                
                let versionIndex = 9
                let reversionIndex = 11
                
                var values = [UInt8](data)
                
                var version = ""
                
                if values.count > versionIndex  {
                    version = "\(values[versionIndex])"
                }
                
                if values.count > reversionIndex {
                    version += String(format: ".%02d", values[reversionIndex])
                }
                self.firmwareVersion = version
                self.delegates.forEach{ $0.peripheral(self, got: version) }
                
                completion(version.isEmpty ? nil : version)
            })
        } catch {
            print(error)
            completion(nil)
        }
    }
    
    public func readSerialNumber(completion: @escaping (String?) -> ()) {
        do {
            try read(.serialNumber, completion: { (data) in
                guard let data = data else { return completion(nil) }
                completion(String(data: data, encoding: .utf8))
            })
        } catch {
            print(error)
            completion(nil)
        }
    }
    
    public func updateFromBinary(data: Data, progress: @escaping (Double) -> ()) {
        DispatchQueue.global(qos: .default).async {
            var bytes = [UInt8](data)
            var result: [UInt8] = []
            
            while bytes.count > 0 {
                let limit = bytes.count > 128 ? 128 : bytes.count
                var array: [UInt8] = [0,0,0,0] + Array(bytes[0..<limit])
                if bytes.count >= 128 {
                    bytes = Array(bytes[limit..<bytes.count])
                } else {
                    bytes.removeAll()
                }
                
                while array.count < 132 {
                    array.append(0xFF)
                }
                result += array
            }
            
            self.update(firmware: result, progress: progress)
        }
    }
    
    public func updateFromBinary(url: URL, progress: @escaping (Double) -> ()) {
        do {
            let data = try Data(contentsOf: url)
            updateFromBinary(data: data, progress: progress)
        } catch {
            print(error)
        }
    }
    
    public enum ChangeType {
        case insert, delete, update
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

extension Locale {
    enum Region: String {
        // Europe
        case Belgium = "BE"
        case Greece	= "EL"
        case Lithuania = "LT"
        case Portugal = "PT"
        case Bulgaria = "BG"
        case Spain = "ES"
        case Luxembourg	= "LU"
        case Romania = "RO"
        case CzechRepublic = "CZ"
        case France	= "FR"
        case Hungary = "HU"
        case Slovenia = "SI"
        case Denmark = "DK"
        case Croatia = "HR"
        case Malta = "MT"
        case Slovakia = "SK"
        case Germany = "DE"
        case Italy = "IT"
        case Netherlands = "NL"
        case Finland = "FI"
        case Estonia = "EE"
        case Cyprus	= "CY"
        case Austria = "AT"
        case Sweden	= "SE"
        case Ireland = "IE"
        case Latvia	= "LV"
        case Poland = "PL"
        case UnitedKingdom = "UK"
        
        //EFTA
        case Iceland = "IS"
        case Norway = "NO"
        case Liechtenstein = "LI"
        case Switzerland = "CH"
        
        //Others
        case Russia = "RU"
        case UnitedStates = "US"
        case undefined
        
        var isEurope: Bool {
            switch self {
            case .Belgium, .Greece, .Lithuania, .Portugal, .Bulgaria, .Spain, .Luxembourg, .Romania, .CzechRepublic, .France, .Hungary, .Slovenia, .Denmark, .Croatia, .Malta, .Slovakia, .Germany, .Italy, .Netherlands, .Finland, .Estonia, .Cyprus, .Austria, .Sweden, .Ireland, .Latvia, .Poland, .UnitedKingdom, .Iceland, .Norway, .Liechtenstein, .Switzerland:
                return true
            default:
                return false
            }
        }
    }
    
    var region: Region {
        guard let code = regionCode else { return .undefined }
        return Region(rawValue: code) ?? .undefined
    }
    
    var txPowerValue: UInt8 {
        return region.isEurope ? 0x00 : 0x04
    }
}


