//
//  Errors.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/2/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation

public enum EllipseError: LocalizedError {
    case missingService(Ellipse.Service)
    case missingCharacteristic(Ellipse.Characteristic)
    case missingChallengeData
    case missingChallengeKey
    case missingPublicKey
    case missingSignedMessage
    case wrongChallengeData(String)
    case accessDenided
    case pinCodeNotFound(String)
    case wrongPinCode([Ellipse.Pin])
    case timeout
    case noAPIfound
    
    public var errorDescription: String? {
        return "EllipseError.\(self)"
    }
}

func ellipse(_ error: EllipseError) -> Error {
    return error
}

extension Error {
    public var isEllipseTimeout: Bool {
        if let error = self as? EllipseError, case .timeout = error {
            return true
        }
        return false
    }
}
