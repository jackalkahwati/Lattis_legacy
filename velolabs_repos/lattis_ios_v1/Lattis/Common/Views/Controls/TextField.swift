//
//  TextField.swift
//  Lattis
//
//  Created by Ravil Khusainov on 01/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import PhoneNumberKit

fileprivate let phoneNumberKit = PhoneNumberKit()

enum ValidationType {
    case none, notEmpty, email, phone, password, limit(Int)
}

@IBDesignable class TextField: UITextField {
    var validation: ValidationType = .none
    @IBInspectable var left: CGFloat = 0 { didSet{ insets.left = left }}
    @IBInspectable var right: CGFloat = 0 { didSet{ insets.right = right }}
    var insets: UIEdgeInsets = .zero {
        didSet {
            layoutIfNeeded()
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        placeholder = placeholder?.localized()
    }
    
    var isValid: Bool {
        guard let text = text else { return false }
        return validation.isValid(text: text)
    }
    
    var cleanText: String? {
        get {
            switch validation {
            case .phone:
                let region = Locale.current.regionCode ?? "US"
                guard let parsed = try? phoneNumberKit.parse(text!, withRegion: region, ignoreType: true) else { return text }
                return "+\(String(parsed.countryCode))\(String(parsed.nationalNumber))"
            default:
                return text
            }
        }
        
        set {
            switch validation {
            case .phone:
                let region = Locale.current.regionCode ?? "US"
                guard let val = newValue, let parsed = try? phoneNumberKit.parse(val, withRegion: region, ignoreType: true) else {
                    text = newValue
                    return
                }
                let number = "+\(String(parsed.countryCode))\(String(parsed.nationalNumber))"
                text = PartialFormatter().formatPartial(number)
            default:
                text = newValue
            }
        }
    }
    
    override func textRect(forBounds bounds: CGRect) -> CGRect {
        return super.textRect(forBounds: bounds).inset(by: insets)
    }
    
    override func editingRect(forBounds bounds: CGRect) -> CGRect {
        return super.editingRect(forBounds: bounds).inset(by: insets)
    }
    
    override func rightViewRect(forBounds bounds: CGRect) -> CGRect {
        var frame = super.rightViewRect(forBounds: bounds)
        frame.origin.x -= insets.right
        return frame
    }
}

private extension CGRect {
    func inset(by insets: UIEdgeInsets) -> CGRect {
        var frm = self
        frm.origin.x += insets.left
        frm.origin.y += insets.top
        frm.size.width -= insets.left + insets.right
        frm.size.height -= insets.top + insets.bottom
        return frm
    }
}

extension ValidationType {
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
        case .phone:
            let region = Locale.current.regionCode ?? "US"
            return (try? phoneNumberKit.parse(text, withRegion: region, ignoreType: true)) != nil
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
    
    func replace(text: String) -> String? {
        switch self {
        case .phone:
            return format(phone: text)
        default:
            return nil
        }
    }
    
    func format(phone: String) -> String {
        let region = Locale.current.regionCode ?? "US"
        guard let parsed = try? phoneNumberKit.parse(phone, withRegion: region, ignoreType: true) else { return phone }
        let number = "+\(String(parsed.countryCode))\(String(parsed.nationalNumber))"
        return PartialFormatter().formatPartial(number)
    }
}

extension String {
    var isValidEmail: Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: self)
    }
}
