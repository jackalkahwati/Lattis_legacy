//
//  KeychainSwift+Ellipse.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/30/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import KeychainSwift

extension KeychainSwift {
    enum Key: String {
        case signedMessage
        case publicKey
    }
    
    func set(value: String, for key: Key) {
        set(value, forKey: key.rawValue)
    }
    
    func get(_ key: Key) -> String? {
        return get(key.rawValue)
    }
    
    func delete(_ key: Key) {
        delete(key.rawValue)
    }
}

extension Ellipse {
    var isShared: Bool {
        guard let borrower = borrower, let userId = KeychainSwift().userId else { return false }
        return borrower.userId == userId
    }
}
