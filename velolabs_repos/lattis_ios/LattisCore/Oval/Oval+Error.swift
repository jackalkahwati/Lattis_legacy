//
//  Oval+Error.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01.11.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI

extension Error {
    var isInvalidEmailLogIn: Bool {
        guard let error = self as? SessionError else { return false }
        switch error.code {
        case .resourceNotFound:
            return true
        default:
            return false
        }
    }
    
    var isPasswordWrong: Bool {
        guard let error = self as? SessionError else { return false }
        switch error.code {
        case .unauthorized:
            return true
        default:
            return false
        }
    }
    
    var isInvalidConfirmationCode: Bool {
        guard let error = self as? SessionError else { return false }
        switch  error.code {
        case .unauthorized, .conflict:
            return true
        default:
            return false
        }
    }
    
    var isInvalidCreditCard: Bool {
        guard let error = self as? SessionError else { return false }
        switch  error.code {
        case .conflict:
            return true
        default:
            return false
        }
    }
    
    var isCardExists: Bool {
        guard let error = self as? ServerError else { return false }
        switch  error.code {
        case 410:
            return true
        default:
            return false
        }
    }
    
    func isHTTP(code: Int) -> Bool {
        guard let error = self as? SessionError else { return false }
        switch code {
        case 404:
            return error.code == .resourceNotFound
        case 409:
            return error.code == .conflict
        default:
            return false
        }
    }
}
