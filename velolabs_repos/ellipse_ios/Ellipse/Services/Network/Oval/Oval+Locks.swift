//
//  Oval+Locks.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/3/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval

protocol LocksNetwork {
    func registration(with macId: String, completion: @escaping (Result<Ellipse, Error>) -> ())
    func update(lock: Ellipse, completion: @escaping (Result<Ellipse, Error>) -> ())
    func locks(completion: @escaping (Result<Ellipse.Groups, Error>) -> ())
    func signLock(with macId: String, completion: @escaping (Result<Ellipse.Key, Error>) -> ())
    
    func firmvare(version: String?, completion: @escaping (Result<[String], Error>) -> ())
    func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ())
    func firmvareChangeLog(for version: String?, completion: @escaping (Result<[String], Error>) -> ())
    func save(pinCode: [Ellipse.Pin], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ())
    func delete(lock: Ellipse, completion: @escaping (Result<Void, Error>) -> ())
    
    func crashDetected(info: Crash.Info, completion: @escaping (Result<Crash, Error>) -> ())
    func send(emergency message: Contact.Emergency, completion: @escaping (Result<Void, Error>) -> ())
    
    //    func theftDetected(info: CrashInfo, completion: @escaping (Crash) -> ())
    //    func confirm(theft theftId: Int, isConfirmed: Bool, completion: @escaping () -> ())
    func share(lock lockId: Int, to contact: Contact, completion: @escaping (Result<Void, Error>) -> ())
    func unshare(lock: Ellipse, completion: @escaping (Result<Void, Error>) -> ())
    func acceptSharing(confirmationCode: String, completion: @escaping (Result<Ellipse, Error>) -> ())
}

extension Ellipse {
    struct Key: Codable {
        let signedMessage: String
        let publicKey: String
    }
    
    struct Groups {
        let own: [Ellipse]
        let shared: [Ellipse]
        let sharedActive: [Ellipse]
        let borrowed: [Ellipse]
        
        var all: [Ellipse] {
            return own + shared + sharedActive + borrowed
        }
    }
    
    fileprivate struct Locks: Codable {
        let userLocks: [Ellipse]
        let sharedLocks: Group
        
        struct Group: Codable {
            let toUser: [Ellipse]
            let byUser: State
            
            struct State: Codable {
                let active: [Ellipse]
                let inactive: [Ellipse]
            }
        }
    }
}

extension Session: LocksNetwork {
    func registration(with macId: String, completion: @escaping (Result<Ellipse, Error>) -> ()) {
        send(.post(json: ["mac_id": macId], api: .locks(.registration)), completion: completion)
    }
    
    func update(lock: Ellipse, completion: @escaping (Result<Ellipse, Error>) -> ()) {
        send(.post(json: ["properties": lock], api: .locks(.updateLock)), completion: completion)
    }
    
    func locks(completion: @escaping (Result<Ellipse.Groups, Error>) -> ()) {
        send(.get(.locks(.usersLocks)), completion: { (result: Result<Ellipse.Locks, Error>) in
            switch result {
            case .success(let locks):
                let groups = Ellipse.Groups(own: locks.userLocks, shared: locks.sharedLocks.byUser.inactive, sharedActive: locks.sharedLocks.byUser.active, borrowed: locks.sharedLocks.toUser)
                completion(.success(groups))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func signLock(with macId: String, completion: @escaping (Result<Ellipse.Key, Error>) -> ()) {
        send(.post(json: ["mac_id": macId], api: .locks(.signedMessageAndPublicKey)), completion: completion)
    }
    
    public func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ()) {
        send(.get(.locks(.firmwareVersions)), completion: completion)
    }
    
    func firmvare(version: String?, completion: @escaping (Result<[String], Error>) -> ()) {
        let params: [String: String?] = ["version": version]
        send(.post(json: params, api: .locks(.firmware)), completion: completion)
    }
    
    public func firmvareChangeLog(for version: String?, completion: @escaping (Result<[String], Error>) -> ()) {
        let params: [String: String?] = ["version": version]
        send(.post(json: params, api: .locks(.firmwareLog)), completion: completion)
    }
    
    func save(pinCode: [Ellipse.Pin], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Codable {
            let macId: String
            let pinCode: [Ellipse.Pin]
        }
        send(.post(json: Params(macId: macId, pinCode: pinCode), api: .locks(.savePinCode)), completion: completion)
    }
    
    func delete(lock: Ellipse, completion: @escaping (Result<Void, Error>) -> ()) {
        let path: API.Path
        if lock.shareId != nil || User.currentId != lock.owner?.userId {
            path = .revokeSharing
        } else {
            path = .deleteLock
        }
        send(.post(json: lock, api: .locks(path)), completion: completion)
    }
    
    func unshare(lock: Ellipse, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: lock, api: .locks(.revokeSharing)), completion: completion)
    }
    
    func crashDetected(info: Crash.Info, completion: @escaping (Result<Crash, Error>) -> ()) {
        send(.post(json: info, api: .locks(.crashDetected)), completion: completion)
    }
    
    func send(emergency message: Contact.Emergency, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: message, api: .locks(.sendEmergencyMessage)), completion: completion)
    }
    
    func share(lock lockId: Int, to contact: Contact, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Codable {
            let lockId: Int
            let contact: Contact
        }
        send(.post(json: Params(lockId: lockId, contact: contact), api: .locks(.share)), completion: completion)
    }
    
    func acceptSharing(confirmationCode: String, completion: @escaping (Result<Ellipse, Error>) -> ()) {
        send(.post(json: ["confirmation_code": confirmationCode], api: .locks(.shareConfirmation)), completion: completion)
    }
}
