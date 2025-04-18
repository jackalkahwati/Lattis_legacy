//
//  OvalAPI.swift
//  Operator (iOS)
//
//  Created by Ravil Khusainov on 11.10.2021.
//

import Foundation
import Combine
import EllipseLock

final class OvalAPI {
    
    static let shared = OvalAPI()
    fileprivate var credentialsCache: [String: EllipseDevice.Credentials] = [:]
    fileprivate var macIdCache: [Int: String] = [:]
    fileprivate var cancellabels = Set<AnyCancellable>()
    
    func save(credential: EllipseDevice.Credentials, for thing: Int) {
        credentialsCache[credential.macId] = credential
        macIdCache[thing] = credential.macId
    }
    
    func credentials(for thing: Int) -> EllipseDevice.Credentials? {
        guard let macId = macIdCache[thing] else { return nil }
        return credentialsCache[macId]
    }
}

extension OvalAPI: NetworkAPI {
    func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ()) {
        guard let cred = credentialsCache[macId] else { return }
        completion(.success((cred.signedMessage, cred.publicKey)))
    }
    
    func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ()) {
        
    }
    
    func firmvare(version: String?, completion: @escaping (Result<[UInt8], Error>) -> ()) {
        
    }
    
    func firmvareChangeLog(for version: String?, completion: @escaping (Result<[String], Error>) -> ()) {
        
    }
    
    func save(pinCode: [String], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ()) {
        
    }
    
    func getPinCode(forLockWith macId: String, completion: @escaping (Result<[String], Error>) -> ()) {
        
    }

}
