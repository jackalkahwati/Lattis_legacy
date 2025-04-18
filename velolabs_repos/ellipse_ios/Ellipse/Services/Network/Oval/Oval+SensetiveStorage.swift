//
//  Oval+SensetiveStorage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/4/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import KeychainSwift
import Oval

private let tokenKey = "rest.token"
private let refreshTokenKey = "refresh.token"
private let userIdKey = "user.id"

extension KeychainSwift: SensetiveStorage {
    public var restToken: String? {
        set {
            if let token = newValue {
                set(token, forKey: tokenKey, withAccess: .accessibleAfterFirstUnlock)
            } else {
                delete(tokenKey)
            }
        }
        get {
            return get(tokenKey)
        }
    }
    
    public var refreshToken: String? {
        set {
            if let token = newValue {
                set(token, forKey: refreshTokenKey, withAccess: .accessibleAfterFirstUnlock)
            } else {
                delete(refreshTokenKey)
            }
        }
        get {
            return get(refreshTokenKey)
        }
    }
    
    public var userId: Int? {
        set {
            if let userId = newValue {
                set(String(userId), forKey: userIdKey, withAccess: .accessibleAfterFirstUnlock)
            } else {
                delete(userIdKey)
            }
        }
        get {
            if let userId = get(userIdKey) {
                return Int(userId)
            }
            return nil
        }
    }
    
    func logout() {
        restToken = nil
        userId = nil
        refreshToken = nil
    }
    
    func login() {
        let rest = restToken
        let user = userId
        let refresh = refreshToken
        clear()
        restToken = rest
        userId = user
        refreshToken = refresh
    }
}

extension User {
    static var currentId: Int? {
        return Session.shared.storage.userId
    }
}
