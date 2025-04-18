//
//  Values.swift
//  
//
//  Created by Ravil Khusainov on 02.02.2020.
//

import Foundation

public extension EllipseBLE {
    enum SecurityValue: UInt8 {
        case unlocked = 0x00
        case locked = 0x01
        case middle = 0x04
        case invalid = 0x08
        case auto = 0xFF
    }
    
    enum PinValue: UInt8 {
        case up = 0x01
        case right = 0x02
        case down = 0x04
        case left = 0x08
    }
    
    enum CommandValue: UInt8 {
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
}
