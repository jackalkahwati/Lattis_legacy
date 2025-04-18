//
//  Oval+BLE.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/18/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import LattisSDK
import Oval

extension Session: Network {
    public func firmvare(version: String?, completion: @escaping (Result<[UInt8], Error>) -> ()) {
        
    }
    
    public func save(pinCode: [String], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ()) {
        
    }
    
    public func getPinCode(forLockWith macId: String, completion: @escaping (Result<[String], Error>) -> ()) {
        
    }
    
    public func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ()) {
        send(.post(json: ["mac_id": macId], api: .locks(.signedMessageAndPublicKey)), completion: { (result: Result<Ellipse.Key, Error>) in
            switch result {
            case .success(let keys):
                completion(.success((keys.signedMessage, keys.publicKey)))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
}

extension Ellipse.Lock {
    func connect(_ handler: EllipseDelegate? = nil) {
        ellipse.isCurrent = true
        if let p = peripheral {
            LockManager.shared.check(peripheral: p)
        }
        peripheral?.connect(handler: handler, secret: String(ellipse.userId))
    }
    
    func delete() {
        if !ellipse.isShared {
            peripheral?.factoryReset(disconnect: true)
        } else {
            peripheral?.disconnect()
            peripheral?.cleanCache()
        }
    }
}

extension Ellipse.Device {
    func connect(_ handler: EllipseDelegate? = nil) {
        ellipse?.isCurrent = true
        let secret = ellipse == nil ? nil : String(ellipse!.userId)
        LockManager.shared.check(peripheral: peripheral)
        peripheral.connect(handler: handler, secret: secret)
        peripheral.subscribe(LockManager.shared)
    }
}

