//
//  InputValidator.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/25/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import PhoneNumberKit

protocol Validatable {
    var inputType: InputValidator.InputType {get}
}

class InputValidator {
    var handle: ((InputType, Bool, Bool) -> ())?
    
    internal var types: Set<InputType>
    fileprivate var storage: Set<InputType> = []
    fileprivate let kit = PhoneNumberKit()
    
    init(_ types: Set<InputType>) {
        self.types = types
    }
    
    func validate(_ text: String, type: InputType) -> Bool {
        var shouldCnange = true
        var fieldIsValid = false
        switch type {
        case .password:
            fieldIsValid = text.count > 7 && text.count < 18
            shouldCnange = text.count < 17
        case .phone:
            guard let region = Locale.current.regionCode else { break }
            let phone = try? kit.parse(text, withRegion: region, ignoreType: true)
            fieldIsValid = phone != nil
        case .email:
            fieldIsValid = text.isValidEmail
        case .code:
            fieldIsValid = text.count >= 6
            shouldCnange = text.count < 7
        default:
            break
        }
        if fieldIsValid {
            storage.insert(type)
        } else {
            storage.remove(type)
        }
        let isValid = storage == types
        handle?(type, fieldIsValid, isValid)
        return shouldCnange
    }
}

extension InputValidator {
    enum InputType {
        case none
        case phone
        case password
        case email
        case code
    }
}
