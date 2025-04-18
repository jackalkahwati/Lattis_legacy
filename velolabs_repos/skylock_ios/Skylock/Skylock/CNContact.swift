//
//  CNContact.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import Contacts

extension CNContact {
    var fullName: String {
        var fullName = ""
        if isKeyAvailable(CNContactGivenNameKey) {
            fullName += givenName
        }
        
        if isKeyAvailable(CNContactFamilyNameKey) {
            if fullName != "" {
                fullName += " "
            }
            
            fullName += familyName
        }
        
        return fullName
    }
    
    var phoneNumber: CNPhoneNumber? {
        if isKeyAvailable(CNContactPhoneNumbersKey) {
            if let phoneNumber = phoneNumbers.first {
                return phoneNumber.value
            }
        }
        
        return nil
    }
}

extension CNPhoneNumber {
    var countryCodeString: String? {
        return value(forKey: "countryCode") as? String
    }
}
