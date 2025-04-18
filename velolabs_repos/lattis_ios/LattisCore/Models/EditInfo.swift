//
//  EditInfo.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 09/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit

struct EditInfo {
    var value: String?
    var placeholder: String?
    let description: String?
    let warning: String?
    let handler: (String) -> ()
    let actionTitle: String
    let validate: (String) -> (String?, Bool)
    
    func updated<T>(_ values: [WritableKeyPath<EditInfo, T>: T]) -> EditInfo {
        var mutable = self
        values.forEach { (key, value) in
            mutable[keyPath: key] = value
        }
        return mutable
    }
}

extension EditInfo {
    static func first(name: String?, handler: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil, placeholder: name, description: "first_name".localized(), warning: nil, handler: handler, actionTitle: "save".localized(), validate: { value in
            guard value.count <= 24 else { return (String(value.prefix(24)), true)}
            return (nil, value.count > 2)
        })
    }
    
    static func last(name: String?, handler: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil, placeholder: name, description: "last_name".localized(), warning: nil, handler: handler, actionTitle: "save".localized(), validate: { value in
            guard value.count <= 24 else { return (String(value.prefix(24)), true)}
            return (nil, value.count > 2)
        })
    }
    
    static func phone(number: String?, handler: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil, placeholder: number, description: "phone_update_note".localized(), warning: "phone_invalid".localized(), handler: handler, actionTitle: "send_verification_code".localized(), validate: { value in
            return (nil, true)
        })
    }
    
    static func email(address: String? = nil, description: String? = nil, handler: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil, placeholder: address, description: description, warning: "email_invalid".localized(), handler: handler, actionTitle: "send_verification_code".localized(), validate: { value in
            return (nil, value.isValidEmail)
        })
    }
    
    static func password(handler: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil, placeholder: "password_hint".localized(), description: nil, warning: "password_invalid".localized(), handler: handler, actionTitle: "save".localized(), validate: { value in
            return (nil, value.count >= 8 && value.count <= 24)
        })
    }
    
    static func conrimationCode(description: String, handler: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil, placeholder: "hint_enter_code".localized(), description: description, warning: "error_confirmation_code".localized(), handler: handler, actionTitle: "submit".localized(), validate: { value in
            guard value.count < 7 else { return (String(value.prefix(6)), true) }
            return (nil, value.count == 6)
        })
    }
    
    static func promoCode(_ action: @escaping (String) -> ()) -> EditInfo {
        return .init(value: nil,
                     placeholder: "00-0000-0000".localized(),
                     description: "enter_promo_code".localized(),
                     warning: "invalid_promo_code".localized(),
                     handler: action,
                     actionTitle: "add_promo_code".localized(),
                     validate: { (code) -> (String?, Bool) in
                        guard code.count < 13 else { return (String(code.prefix(12)), true) }
                        return (nil, code.count == 12)
                     })
    }
}
