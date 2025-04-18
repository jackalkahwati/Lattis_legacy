//
//  KeychainSwift+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 24/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import Foundation
import KeychainSwift

extension KeychainSwift {
    enum Key: String {
        case signedMessage = "signed.message"
        case publicKey = "public.key"
        case password = "password"
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
}
