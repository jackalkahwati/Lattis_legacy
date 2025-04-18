//
//  KeychainSwift+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import KeychainSwift

extension KeychainSwift {
    enum Key: String {
        case signedMessage = "signed.message"
        case publicKey = "public.key"
        case password = "password"
        case userId = "user.id"
    }
    
    func get(_ key: Key) -> String? {
        return get(key.rawValue)
    }
    
    func set(_ value: String, forKey key: Key) {
        set(value, forKey: key.rawValue)
    }
    
    func setWithAccess(_ value: String, forKey key: Key, access: KeychainSwiftAccessOptions?) {
        set(value, forKey: key.rawValue, withAccess: access)
    }
    
    func delete(_ key: Key) {
        delete(key.rawValue)
    }
    
    static func clean(for macId: String) {
        let chain = KeychainSwift(keyPrefix: macId)
        chain.delete(.publicKey)
        chain.delete(.signedMessage)
    }
}
