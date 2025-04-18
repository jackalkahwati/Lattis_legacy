//
//  UserDefaultsBacked.swift
//  
//
//  Created by Ravil Khusainov on 29.01.2020.
//

import Foundation

@propertyWrapper public struct UserDefaultsBacked<Value> {
    public let key: String
    public let defaultValue: Value
    public var storage: UserDefaults = .standard
    
    public init(key: String, defaultValue: Value, storage: UserDefaults = .standard) {
        self.key = key
        self.defaultValue = defaultValue
        self.storage = storage
    }
    
    public var wrappedValue: Value {
        set {
            if let optional = newValue as? AnyOptional, optional.isNil {
                storage.removeObject(forKey: key)
            } else {
                storage.setValue(newValue, forKey: key)
            }
        }
        get {
            let value = storage.value(forKey: key) as? Value
            return value ?? defaultValue
        }
    }
}

public extension UserDefaultsBacked where Value: ExpressibleByNilLiteral {
    init(key: String, storage: UserDefaults = .standard) {
        self.init(key: key, defaultValue: nil, storage: storage)
    }
}


private protocol AnyOptional {
    var isNil: Bool { get }
}

extension Optional: AnyOptional {
    var isNil: Bool { self == nil }
}
