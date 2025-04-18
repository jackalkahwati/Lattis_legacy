//
//  Key.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 9/21/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation
@_implementationOnly
import KeychainSwift

fileprivate let anchornsKey = "LattisSDK.Key.Anchorn"
fileprivate let publicKeyKey = "LattisSDK.Key.publicKey"
fileprivate let signedMessageKey = "LattisSDK.Key.signedMessage"
fileprivate let challengeKeyKey = "LattisSDK.Key.challengeKey"

struct Key {
    let signedMessage: String
    let challengeKey: String
}

extension Key {
    init?(_ macId: String) {
        guard let key = Storage.shared.key(macId) else { return nil }
        self.init(signedMessage: key.signedMessage, challengeKey: key.challengeKey)
    }
    
    func save(_ macId: String) {
        Storage.shared.save(self, macId: macId)
    }
    
    static func clean() {
        Storage.shared.clean()
    }
    
    static func remove(_ macId: String) {
        Storage.shared.remove(macId: macId)
    }
}

fileprivate extension Key {
    struct Anchor {
        let date: Date
        let macId: String
    }
    final class Storage {
        static let shared = Storage()
        var cache: [String: Date] = [:]
        let flash = UserDefaults.standard
        
        init() {
            if let c = flash.dictionary(forKey: anchornsKey) as? [String: Date] {
                cache = c
            }
        }
        
        func key(_ macId: String) -> Key? {
            let keychain = KeychainSwift(keyPrefix: "Ellipse." + macId)
            keychain.delete(publicKeyKey)
            guard let sMess = keychain.signedMessage,
                let cKey = keychain.challengeKey,
                let date = date(macId),
                date.isExpired == false else {
                    keychain.signedMessage = nil
                    keychain.challengeKey = nil
                    remove(macId: macId)
                    return nil
            }
            return Key(signedMessage: sMess, challengeKey: cKey)
        }
        
        func date(_ macId: String) -> Date? {
            return cache[macId]
        }
        
        func save(_ key: Key, macId: String) {
            guard EllipseManager.uninitialized.cashingStrategy == .default else { return }
            let keychain = KeychainSwift(keyPrefix: "Ellipse." + macId)
            keychain.signedMessage = key.signedMessage
            keychain.challengeKey = key.challengeKey
            
            save(anchor: .init(date: Date(), macId: macId))
        }
        
        func save(anchor: Anchor) {
            cache[anchor.macId] = anchor.date
            synchronize()
        }
        
        func remove(macId: String) {
            let keychain = KeychainSwift(keyPrefix: "Ellipse." + macId)
            keychain.signedMessage = nil
            keychain.challengeKey = nil
            cache.removeValue(forKey: macId)
            synchronize()
        }
        
        func synchronize() {
            flash.setValue(cache, forKey: anchornsKey)
            flash.synchronize()
        }
        
        func clean() {
            let copy = cache
            for (macId, date) in copy where date.isExpired {
                cache.removeValue(forKey: macId)
                let keychain = KeychainSwift(keyPrefix: macId)
                keychain.signedMessage = nil
                keychain.challengeKey = nil
            }
            synchronize()
        }
    }
}

fileprivate extension KeychainSwift {
    var signedMessage: String? {
        set {
            if let value = newValue {
                set(value, forKey: signedMessageKey)
            } else {
                delete(signedMessageKey)
            }
        }
        get {
            return get(signedMessageKey)
        }
    }
    
    var challengeKey: String? {
        set {
            if let value = newValue {
                set(value, forKey: challengeKeyKey)
            } else {
                delete(challengeKeyKey)
            }
        }
        get {
            return get(challengeKeyKey)
        }
    }
}

#if DEBUG
fileprivate let limit: Double = 1 // 1 sec
#else
fileprivate let limit: Double = 86400 // 24 hours
#endif

fileprivate extension Date {
    var isExpired: Bool {
        return false // Keys will never expire. Testing pupose
//        return abs(timeIntervalSinceNow) > limit
    }
}
