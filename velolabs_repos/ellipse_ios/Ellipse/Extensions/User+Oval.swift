//
//  User+Oval.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/13/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

extension User {
    struct Credentials: Codable {
        let countryCode = Locale.current.regionCode ?? "us"
        var userType: UserType = .ellipse
        var phone: String?
        var password: String?
        var email: String?
        var usersId: String?
        var isSigningUp: Bool = false
        var facebookToken: String?
        var firstName: String?
        var lastName: String?
        var regId: String = AppDelegate.shared.pushToken
        
        init(phone: String? = nil, password: String? = nil, email: String? = nil) {
            self.phone = phone
            self.password = password
            self.email = email
            self.usersId = phone
        }
        
        init(_ facebook: FacebookHelper.User) {
            self.phone = nil
            self.password = facebook.id
            self.usersId = facebook.id
            self.email = facebook.email
            self.facebookToken = facebook.token
            self.firstName = facebook.firstName
            self.lastName = facebook.lastName
            self.isSigningUp = true
            self.userType = .facebook
        }
        
        enum CodingKeys: String, CodingKey {
            case email
            case regId
            case userType
            case isSigningUp
            case countryCode
            case password
            case usersId
            case phone = "phoneNumber"
            case firstName
            case lastName
            case facebookToken
        }
    }
}
