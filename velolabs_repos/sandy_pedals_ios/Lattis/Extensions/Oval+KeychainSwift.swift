//
//  Oval+KeychainSwift.swift
//  Lattis
//
//  Created by Ravil Khusainov on 9/11/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import Oval
import KeychainSwift

private let tokenKey = "rest.token"
private let refreshTokenKey = "refresh.token"
private let userIdKey = "user.id"

extension KeychainSwift: SensetiveStorage {
    public var secret: String? {
        get {
            return nil
        }
        set {}
    }
    
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
}


