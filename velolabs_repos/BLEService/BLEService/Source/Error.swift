//
//  Error.swift
//  BLEService
//
//  Created by Ravil Khusainov on 5/25/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

public extension Peripheral {
    public enum Error: Swift.Error {
        case missingService(Service)
        case missingCharacteristic(Characteristic)
        case wrongPinCode([Pin])
        case accessDenided
        case connectionTimeout
        case reconnectionTimeout
        case missingUserId
        case missingPublicKey
        case missingSignedMessage
    }
}
