//
//  KeychainBacked.swift
//  
//
//  Created by Ravil Khusainov on 12.03.2021.
//

import Foundation
import KeychainSwift

@propertyWrapper public struct KeychainBacked {
    
    public let key: String
    private let keychain: KeychainSwift
    
    public init(key: String, prefix: String? = nil) {
        self.key = key
        if let p = prefix {
            self.keychain = .init(keyPrefix: p)
        } else {
            self.keychain = .init()
        }
    }
    
    public var wrappedValue: String? {
        get {
            return keychain.get(key)
        }
        set {
            if let value = newValue {
                keychain.set(value, forKey: key)
            } else {
                keychain.delete(key)
            }
        }
    }
}


