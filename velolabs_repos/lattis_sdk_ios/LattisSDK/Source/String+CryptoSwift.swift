//
//  String+CryptoSwift.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/2/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import CryptoSwift

extension String {
    var challengeKeyValue: String {
        var value = self.md5()
        while value.count < 64 {
            value.append("f")
        }
        return value.lowercased()
    }
    
    public var dataValue: Data? {
        guard let bytes = bytesArray else { return nil }
        return Data(bytes)
    }
    
    public var bytesArray: [UInt8]? {
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

public extension Data {
    var ellipseVersion: String? {
        return version(versionIndex: 9, reversionIndex: 11)
    }
    
    var bootLoaderVersion: String? {
        return version(versionIndex: 5, reversionIndex: 7)
    }
    
    private func version(versionIndex: Int, reversionIndex: Int) -> String? {
        var values = [UInt8](self)
        
        guard values.count > versionIndex else { return nil }
        var version = "\(values[versionIndex])"
        
        guard values.count > reversionIndex else { return version }
        version += String(format: ".%02d", values[reversionIndex])
        
        return version
    }
    
    var hex: String {
        return map { String(format: "%02x", $0) }.joined(separator: "")
    }
}
