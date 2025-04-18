//
//  OvalUser.swift
//  OvalApi
//
//  Created by Ravil Khusainov on 20/01/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import SwiftyJSON
import KeychainSwift

public extension Oval {
    public static let users = Users()
    public class Users: OvalRoute {
        public typealias success = (Responce) -> ()
        internal let basePath = "users/"
        
        public func registration(user: Request, success: @escaping (Int32, Bool) -> (), fail: @escaping fail) {
            KeychainSwift().clear()
            let reg = Oval.post(path: basePath + "registration/", post: user.params)
            let parse: (JSON) -> (Int32, Bool)? = { json in
                guard let userId = json["user_id"].int32, let isVerified = json["verified"].bool else { return nil }
                return (userId, isVerified)
            }
            perform(resource: reg, parse: parse, fail: fail, mapped: { userId, isVerified in
                Oval.userId = userId
                success(userId, isVerified)
            })
        }
        
        public func getTokens(userId: Int32, password: String, success: @escaping () -> (), fail: @escaping fail) {
            let reg = Oval.post(path: basePath + "new-tokens/", post: ["user_id": userId, "password": password])
            let parse: (JSON) -> (String, String)? = { json in
                if let restToken = json["rest_token"].string, let refreshToken = json["refresh_token"].string {
                    return (restToken, refreshToken)
                }
                return nil
            }
            perform(resource: reg, parse: parse, fail: fail) { response in
                Oval.restToken = response.0
                Oval.refreshToken = response.1
                success()
            }
        }
        
        public func signInCode(success: @escaping () -> (), fail: @escaping fail) {
            guard let userId = Oval.userId else { return }
            let request = Oval.post(path: basePath + "sign-in-code/", post: ["user_id": userId])
            let retry = { self.signInCode(success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func confirm(signIn code: String, success: @escaping () -> (), fail: @escaping fail) {
            guard let userId = Oval.userId else { return }
            let request = Oval.post(path: basePath + "confirm-user-code/", post: ["confirmation_code": code, "user_id": userId])
            perform(resource: request, parse: Responce.init, fail: fail) { (responce) in
                Oval.restToken = responce.restToken
                Oval.refreshToken = responce.refreshToken
                success()
            }
        }
        
        public func forgotPassword(phone: String, success: @escaping () -> (), fail: @escaping fail) {
            KeychainSwift().clear()
            let request = Oval.post(path: basePath + "forgot-password-code/", post: ["phone_number": phone])
            perform(resource: request, check: 200, fail: fail, checked: success)
        }
        
        public func confirm(forgot code: String, phone: String, password: String, success: @escaping () -> (), fail: @escaping fail) {
            let request = Oval.post(path: basePath + "confirm-forgot-password-code/", post: ["confirmation_code": code, "phone_number": phone, "password": password])
            perform(resource: request, check: 200, fail: fail, checked: success)
        }
        
        public func confirm(email: String?, code: String, accountType: String = "main_account", success: @escaping (Responce) -> (), fail: @escaping Oval.fail) {
            guard let userId = Oval.userId else { return }
            var params: [String: Any] = ["user_id": userId, "confirmation_code": code, "account_type": accountType]
            if let email = email {
                params["email"] = email
            }
            let request = Oval.post(path: basePath + "confirm-email-verification-code/", post: params)
            perform(resource: request, parse: Responce.init, fail: fail) {  responce in
                
                // FIXME: Very bad code! Need to fix that smell architecture
                if let token = responce.restToken {
                    Oval.restToken = token
                }
                if let token = responce.refreshToken {
                    Oval.refreshToken = token
                }
                success(responce)
            }
        }
        
        public func user(withId userId: Int32? = nil, success: @escaping success, fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let user = Oval.post(path: basePath + "get-user/", post: userId == nil ? [:] : ["user_id": userId!], restToken: token)
            let retry = { self.user(withId: userId, success: success, fail: fail) }
            perform(resource: user, parse: Responce.init, retry: retry, fail: fail, mapped: success)
        }
        
        public func update(user: Request, success: @escaping success, fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "update-user/", post: user.properties, restToken: token)
            let retry = { self.update(user: user, success: success, fail: fail) }
            perform(resource: request, parse: Responce.init, retry: retry, fail: fail, mapped: success)
        }
        
        public func getUpdatePasswordCode(success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.get(path: basePath + "update-password-code/", restToken: token)
            let retry = { self.getUpdatePasswordCode(success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func update(password: String, with confirmationCode: String, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "update-password/",post: ["password": password, "confirmation_code": confirmationCode],restToken: token)
            let retry = { self.update(password: password, with: confirmationCode, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func getUpdateCode(for phoneNumber: String, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "update-phone-number-code/",post: ["phone_number": phoneNumber], restToken: token)
            let retry = { self.getUpdateCode(for: phoneNumber, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func update(phoneNumber: String, with confirmationCode: String, success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "update-phone-number/",post: ["phone_number": phoneNumber, "confirmation_code": confirmationCode],restToken: token)
            let retry = { self.update(phoneNumber: phoneNumber, with: confirmationCode, success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func getTermsAndConditions(success: @escaping (String, String)-> Void, fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.get(path: basePath + "terms-and-conditions/", restToken: token)
            let retry = { self.getTermsAndConditions(success: success, fail: fail) }
            let parse: (JSON) -> (String, String)? = { payload in
                let dict = payload.dictionary
                guard let terms = dict?["terms_and_conditions"], let version = terms["version"].string, let body = terms["terms"].string else { return nil }
                return (version, body)
            }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
        
        public func acceptTermsAndConditions(success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.post(path: basePath + "accept-terms-and-conditions/", post: ["did_accept": true], restToken: token)
            let retry = { self.acceptTermsAndConditions(success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func delete(success: @escaping () -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.get(path: basePath + "delete-account/", restToken: token)
            let retry = { self.delete(success: success, fail: fail) }
            perform(resource: request, check: 200, retry: retry, fail: fail, checked: success)
        }
        
        public func checkTerms(success: @escaping (Bool) -> (), fail: @escaping fail) {
            guard let token = Oval.restToken else { return fail(Error.invalidToken) }
            let request = Oval.get(path: basePath + "check-accepted-terms-and-conditions/", restToken: token)
            let retry = { self.checkTerms(success: success, fail: fail) }
            let parse: (JSON) -> Bool? = { $0["has_accepted"].bool }
            perform(resource: request, parse: parse, retry: retry, fail: fail, mapped: success)
        }
    }
}

public extension Oval.Users {
    public enum UserType: String {
        case facebook, ellipse, lattis, privateAccount
    }
    
    public struct Request {
        public var usersId: String?
        public var userId: Int32?
        public var firstName: String?
        public var lastName: String?
        public var regId: String?
        public var userType: UserType?
        public var phoneNumber: String?
        public var password: String?
        public var isSigningUp: Bool?
        public var countryCode: String?
        public var email: String?
        public var facebookToken: String?
        public init(usersId: String? = nil, userId: Int32? = nil, firstName: String? = nil, lastName: String? = nil, regId: String? = nil, userType: UserType? = nil, phoneNumber: String? = nil, password: String? = nil, isSigningUp: Bool? = nil, countryCode: String? = nil, email: String? = nil, facebookToken: String? = nil) {
            self.usersId = usersId
            self.userId = userId
            self.firstName = firstName
            self.lastName = lastName
            self.regId = regId
            self.userType = userType
            self.phoneNumber = phoneNumber
            self.password = password
            self.isSigningUp = isSigningUp
            self.countryCode = countryCode
            self.email = email
            self.facebookToken = facebookToken
        }
    }
    
    public struct Responce {
        public let userId: Int32
        public let usersId: String?
        public let username: String?
        public let userType: UserType
        public let isVerified: Bool
        public let maxLocks: Int32
        public let title: String?
        public let firstName: String?
        public let lastName: String?
        public let phoneNumber: String?
        public let email: String?
        public let countryCode: String?
        fileprivate let restToken: String?
        fileprivate let refreshToken: String?
    }
}

public extension Oval.Users.Request {
    public init(response: Oval.Users.Responce) {
        usersId = response.usersId
        userId = response.userId
        firstName = response.firstName
        lastName = response.lastName
        userType = response.userType
        phoneNumber = response.phoneNumber
        countryCode = response.countryCode
        email = response.email
    }
    
    public var params: [String: Any] {
        var dict: [String: Any] = [:]
        if let regId = regId {
            dict["reg_id"] = regId
        }
        if let usersId = usersId {
            dict["users_id"] = usersId
        }
        if let userType = userType {
            dict["user_type"] = userType.rawValue
        }
        if let phoneNumber = phoneNumber {
            dict["phone_number"] = phoneNumber
        }
        if let password = password {
            dict["password"] = password
        }
        if let isSigningUp = isSigningUp {
            dict["is_signing_up"] = isSigningUp
        }
        if let countryCode = countryCode {
            dict["country_code"] = countryCode
        }
        if let email = email {
            dict["email"] = email
        }
        if let userId = userId {
            dict["user_id"] = userId
        }
        if let firstName = firstName {
            dict["first_name"] = firstName
        }
        if let lastName = lastName {
            dict["last_name"] = lastName
        }
        if let facebookToken = facebookToken {
            dict["facebook_token"] = facebookToken
        }
        return dict
    }
    
    public var properties: [String: Any] {
        return ["properties": params]
    }
}

public extension Oval.Users.Responce {
    public init?(_ json: JSON) {
        guard let userId = json["user_id"].int32 else { return nil }
//            let usersId = json["users_id"].string,
//            let userTypeString = json["user_type"].string,
//            let userType = Oval.Users.UserType(rawValue: userTypeString) else { return nil }
        self.userId = userId
        self.usersId = json["users_id"].string
        self.restToken = json["rest_token"].string
        self.username = json["username"].string
        if let userTypeString = json["user_type"].string,
            let userType = Oval.Users.UserType(rawValue: userTypeString) {
            self.userType = userType
        } else {
            self.userType = .privateAccount
        }
        
        self.isVerified = json["verified"].bool ?? false
        self.maxLocks = json["max_locks"].int32 ?? 0
        self.title = json["title"].string
        self.firstName = json["first_name"].string
        self.lastName = json["last_name"].string
        self.phoneNumber = json["phone_number"].string
        self.email = json["email"].string
        self.countryCode = json["country_code"].string
        self.refreshToken = json["refresh_token"].string
    }
}
