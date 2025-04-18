//
//  Bit.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 20/03/2019.
//  Copyright © 2019 Lattis Inc. All rights reserved.
//

import Foundation

enum Bit: UInt8 {
    case zero
    case one
}

extension Bit {
    struct Position {
        let value: Int
        
        static func capTouch(_ value: CapTouch) -> Position {
            return .init(value: value.rawValue)
        }
        
        enum CapTouch: Int {
            case bounding = 7
            case magneticAutoLock = 6
            case disableFactoryResetPin = 5
            case disableCapTouch = 4
            case increaseOpenTime = 0
        }
    }
}

extension UInt8 {
    var bits: [Bit] {
        var byte = self
        var bits = [Bit](repeating: .zero, count: 8)
        for i in 0..<8 {
            let currentBit = byte & 0x01
            if currentBit != 0 {
                bits[i] = .one
            }
            
            byte >>= 1
        }
        
        return bits
    }
    
    func isBitSet(position: Bit.Position) -> Bool {
        let bits = self.bits
        let pos = position.value
        guard bits.count > pos else { return false }
        return bits[pos] == .one
    }
    
    func replaced(bit: Bit, in position: Bit.Position) -> UInt8 {
        var bits = self.bits
        let pos = position.value
        guard bits.count > pos else { return self }
        bits[pos] = bit
        guard let result = bits.bytes.first else { return self }
        return result
    }
}

extension Array where Element == Bit {
    var bytes: [UInt8] {
        let numBits = count
        let numBytes = (numBits + 7)/8
        var bytes = [UInt8](repeating : 0, count : numBytes)
        
        for (index, bit) in reversed().enumerated() {
            if bit == .one {
                bytes[index / 8] += 1 << (7 - index % 8)
            }
        }
        
        return bytes
    }
}
