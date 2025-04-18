//
//  User.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation

struct User: Codable {
    let userId: Int
    var firstName: String
    var lastName: String
    let email: String
    let phoneNumber: String?
    // Delete message
    var msg: String?
    
    func updated<T>(key: WritableKeyPath<User, T>, value: T) -> User {
        var mutable = self
        mutable[keyPath: key] = value
        return mutable
    }
}

extension User {
    struct Update: Decodable {
        let user: User
        let fleets: [Fleet]
        
        enum CodingKeys: String, CodingKey {
            case user
            case fleets = "privateAccount"
        }
    }
    
    struct LogIn: Encodable {
        let email: String
        let password: String
        let isSigningUp: Bool
        let usersId: String
        let userType: String
        let regId: String
        let firstName: String?
        let lastName: String?
        let device_language: String
        
        init(email: String, password: String) {
            self.email = email
            self.usersId = email
            self.password = password
            self.isSigningUp = false
            self.userType = "lattis"
            self.firstName = nil
            self.lastName = nil
            self.regId = AppRouter.shared.notificationToken
            self.device_language = UIDevice.current.deviceLanguage
        }
        
        init(email: String, password: String, firstName: String?, lastName: String?) {
            self.email = email
            self.usersId = email
            self.password = password
            self.isSigningUp = true
            self.userType = "lattis"
            self.firstName = firstName
            self.lastName = lastName
            self.regId = AppRouter.shared.notificationToken
            self.device_language = UIDevice.current.deviceLanguage
        }
    }
}

extension User {
    var fullName: String {
        return "\(firstName) \(lastName)"
    }
}
