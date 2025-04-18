//
//  BLEService+Lattis.swift
//  Lattis
//
//  Created by Ravil Khusainov on 29/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import LattisSDK
import Oval
import KeychainSwift


public struct Empty: Codable {}
extension Request {
    static func emptyPost(_ api: API) -> Request<Empty> {
        return .post(json: Empty(), api: api)
    }
}

extension LattisSDK.Ellipse {
    func connect(handler: EllipseDelegate?, bike: Bike) {
        bikeId = bike.bikeId
        connect(handler: handler, secret: bike.fleetKey)
    }
}

fileprivate var bikeId: Int?

extension API {
    static let signedMessage = API(path: "locks/signed-message-and-public-key-for-trip")
}

extension Session: Network {
    public func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ()) {
        let chain = KeychainSwift(keyPrefix: macId)
        if let m = chain.get(.signedMessage), let k = chain.get(.publicKey) {
            return completion(.success((m, k)))
        }
        guard let bikeId = bikeId else { return }
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
