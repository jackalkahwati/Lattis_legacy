//
//  TextValidator.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 6/7/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

enum TextValidator {
    case none, notEmpty, email, password, limit(Int)
}

extension TextValidator {
    func isValid(text: String) -> Bool {
        switch self {
        case .password:
            return text.count >= 8
        case .notEmpty:
            return text.isEmpty == false
        case .email:
            return text.isValidEmail
        case .limit(let value):
            return text.count == value
        default:
            return true
        }
    }
    
    func canCahnge(to text: String) -> Bool {
        switch self {
        case .password:
            return text.count <= 20
        case .limit(let value):
            return text.count <= value
        default:
            return true
        }
    }
}

extension String {
    var isValidEmail: Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: self)
    }
}
