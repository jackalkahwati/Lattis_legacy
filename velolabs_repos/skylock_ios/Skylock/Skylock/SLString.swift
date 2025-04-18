//
//  SLString.swift
//  Skylock
//
//  Created by Andre Green on 6/17/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation

extension String {
    func trimmedWhiteSpaces() -> String {
        return self.trimmingCharacters(in: .whitespaces)
    }
    
    func macAddress() -> String? {
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
    
    func bytesString() -> Data? {
        let count = self.characters.count
        if count % 2 == 1 {
            print("cannot convert \(self) to bytes string. Odd number of digits.")
            return nil
        }
        
        var bytes:[UInt8] = []
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
        
        return Data(bytes: bytes)
    }
}
