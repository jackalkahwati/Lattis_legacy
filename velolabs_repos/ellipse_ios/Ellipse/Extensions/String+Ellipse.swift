//
//  Strin+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 25/01/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import Foundation
import PhoneNumberKit

extension PhoneNumberKit {
    static let shared = PhoneNumberKit()
    static let formatter = PartialFormatter()
}

extension String {
    var phoneNumberPlain: String {
//        let region = Locale.current.regionCode ?? "US"
        guard let phone = try? PhoneNumberKit.shared.parse(self.trimmingCharacters(in: .whitespacesAndNewlines), ignoreType: true) else { return self }
        return "+\(String(phone.countryCode))\(String(phone.nationalNumber))"
    }
    
    var phoneNumberFormat: String {
        return PhoneNumberKit.formatter.formatPartial(phoneNumberPlain)
    }
    
    var isValidPhoneNumber: Bool {
        guard let _ = try? PhoneNumberKit.shared.parse(self.trimmingCharacters(in: .whitespacesAndNewlines), ignoreType: true) else { return false }
        return true
    }
    
    var isValidEmail: Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: self)
    }
}
