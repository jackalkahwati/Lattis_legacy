//
//  SensetiveStorage.swift
//  OvalExample
//
//  Created by Ravil Khusainov on 7/10/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import Foundation

public protocol SensetiveStorage {
    var restToken: String? {get set}
    var refreshToken: String? {get set}
    var userId: Int? {get set}
}

fileprivate let restTokenKey = "restTokenKey"
fileprivate let refreshTokenKey = "refreshTokenKey"
fileprivate let userIdKey = "userIdKey"

extension UserDefaults: SensetiveStorage {
    
    public var restToken: String? {
        get {
            return string(forKey: restTokenKey)
        }
        set {
            set(newValue, forKey: restTokenKey)
            synchronize()
        }
    }
    
    public var refreshToken: String? {
        get {
            return string(forKey: refreshTokenKey)
        }
        set {
            set(newValue, forKey: refreshTokenKey)
            synchronize()
        }
    }
    
    public var userId: Int? {
        get {
            let res = integer(forKey: userIdKey)
            return res > 0 ? res : nil
        }
        set {
            set(newValue, forKey: userIdKey)
            synchronize()
        }
    }
}
