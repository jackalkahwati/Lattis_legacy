//
//  File.swift
//  
//
//  Created by Ravil Khusainov on 06.04.2022.
//

import Foundation
import CoreBluetooth

extension Data {
    func hexEncodedString() -> String {
        return map { String(format: "%02hhx", $0) }.joined()
    }
}

extension String {
    
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

extension CBUUID {
    static var sas: CBUUID {
        .init(string: "a5630100-5d20-465f-b493-6a0031b9fcf3".uppercased())
    }
}

extension CBCharacteristic {
    var sas: SasBLE.Characteristic? {
        .init(rawValue: uuid.uuidString.lowercased())
    }
}

extension Int {
    func `in`(_ array: [Self]) -> Bool {
        return array.contains(self)
    }
}

