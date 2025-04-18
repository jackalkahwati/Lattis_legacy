//
//  Oval+Ellipse.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 26.09.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import OvalAPI
import EllipseLock
import KeychainSwift

extension EllipseManager {
    static var shared: EllipseManager { uninitialized }
}

fileprivate extension API {
    static let signedMessage = API(path: "locks/signed-message-and-public-key-for-trip")
}

var connectBikeId: Int?

extension Session: NetworkAPI {
    public func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ()) {
        let chain = KeychainSwift(keyPrefix: macId)
        if let m = chain.get(.signedMessage), let k = chain.get(.publicKey) {
            return completion(.success((m, k)))
        }
        guard let bikeId = connectBikeId else { return }
        let params: [String: Int] = ["bike_id": bikeId]
        struct Res: Decodable {
            let signedMessage: String
            let publicKey: String
        }
        send(.post(json: params, api: .signedMessage)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                chain.set(res.signedMessage, forKey: .signedMessage)
                chain.set(res.publicKey, forKey: .publicKey)
                completion(.success((res.signedMessage, res.publicKey)))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    public func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ()) {
        
    }
    
    public func firmvare(version: String?, completion: @escaping (Result<[UInt8], Error>) -> ()) {
        
    }
    
    public func firmvareChangeLog(for version: String?, completion: @escaping (Result<[String], Error>) -> ()) {
        
    }
    
    public func save(pinCode: [String], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ()) {
        
    }
    
    public func getPinCode(forLockWith macId: String, completion: @escaping (Result<[String], Error>) -> ()) {
        
    }
}

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
