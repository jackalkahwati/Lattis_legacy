//
//  LattisSDK+Oval.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/12/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval
import LattisSDK

typealias Peripheral = LattisSDK.Ellipse
var fleetId: Int!

extension LattisSDK.Ellipse {
    func connect(lock: Ellipse, handler: EllipseDelegate? = nil) {
        fleetId = lock.fleetId
        connect(handler: handler, secret: lock.fleetKey)
    }
}


extension Session: Network {
    public func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ()) {
        let api = API(path: "locks/signed-message-and-public-key-for-operator")
        struct Params: Encodable {
            let macId: String
            let fleetId: Int
        }
        let params = Params(macId: macId, fleetId: fleetId!)
        struct Res: Decodable {
            let signedMessage: String
            let publicKey: String
        }
        send(.post(json: params, api: api)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success((res.signedMessage, res.publicKey)))
            case .failure(let e):
                completion(.failure(e))
            }
        }
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

