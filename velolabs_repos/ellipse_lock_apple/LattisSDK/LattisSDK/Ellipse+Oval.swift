//
//  Ellipse+Oval.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/3/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Foundation

extension EllipseManager {
    @objc
    public static var shared: EllipseManager {
        if uninitialized.api == nil {
            uninitialized.api = Session()
        }
        return uninitialized
    }
}

fileprivate let lattisEndpoint = "https://lattisappv2.lattisapi.io/api"
fileprivate var fleetId: Int? = nil
fileprivate var headers: [String: String] {
    guard let token = Bundle.main.infoDictionary?["LattisSDKToken"] as? String  else {
        fatalError("Error: 'LattisSDKToken' key is not specified in info.plist")
    }
    return ["api-token": token]
}

fileprivate extension API {
    static let firmvareVersions = API(endpoint: lattisEndpoint, path: "locks/firmware-versions-for-operator")
    static let firmvare = API(endpoint: lattisEndpoint, path: "locks/firmware-for-operator")
    static let firmvareLog = API(endpoint: lattisEndpoint, path: "locks/firmware-log-for-operator")
    static let registration = API(endpoint: lattisEndpoint, path: "locks/registration-for-operator")
    static let signedMessage = API(endpoint: lattisEndpoint, path: "locks/signed-message-and-public-key-for-operator")
    static let fleet = API(endpoint: lattisEndpoint, path: "fleet/fleet-of-token")
    static let pinCode = API(endpoint: lattisEndpoint, path: "locks/save-pin-code")
    static let locks = API(endpoint: lattisEndpoint, path: "locks/get-locks")
}

extension Session: NetworkAPI {
    public func sign(lockWith macId: String, completion: @escaping (Result<(String, String), Error>) -> ()) {
        if let id = fleetId, EllipseManager.secret != nil {
            struct Params: Codable {
                let macId: String
                let fleetId: Int
            }
            struct Res: Codable {
                let publicKey: String
                let signedMessage: String
            }
            let handle: (Result<Res, Error>) -> () = { result in
                switch result {
                case .success(let result):
                    completion(.success((result.signedMessage, result.publicKey)))
                case .failure(let error):
                    completion(.failure(error))
                }
                
            }
            send(.post(json: Params(macId: macId, fleetId: id), api: .registration, headers: headers), completion: handle)
        } else {
            updateFleet { result in
                switch result {
                case .success:
                    self.sign(lockWith: macId, completion: completion)
                case .failure(let error):
                    completion(.failure(error))
                }
                
            }
        }
    }
    
    public func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ()) {
        send(.get(.firmvareVersions, headers: headers), completion: completion)
    }
    
    public func firmvare(version: String?, completion: @escaping (Result<[UInt8], Error>) -> ()) {
        var params: [String: String] = [:]
        if let v = version {
            params["version"] = v
        }
        let convert: (Result<[String], Error>) -> () = { result in
            switch result {
            case .success(let stringValue):
                completion(.success(stringValue.compactMap({$0.bytesArray}).flatMap({$0})))
            case .failure(let error):
                completion(.failure(error))
            }
            
        }
        send(.post(json: params, api: .firmvare, headers: headers), completion: convert)
    }
    
    public func firmvareChangeLog(for version: String?, completion: @escaping (Result<[String], Error>) -> ()) {
        var params: [String: String] = [:]
        if let v = version {
            params["version"] = v
        }
        send(.post(json: params, api: .firmvareLog, headers: headers), completion: completion)
    }
    
    public func save(pinCode: [String], forLock macId: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Codable {
            let macId: String
            let pinCode: [String]
        }
        let params = Params(macId: macId, pinCode: pinCode)
        send(.post(json: params, api: .pinCode, headers: headers), completion: completion)
    }
    
    public func getPinCode(forLockWith macId: String, completion: @escaping (Result<[String], Error>) -> ()) {
        struct Ellipse: Codable {
            let macId: String
            let pinCode: [String]?
        }
        struct Locks: Codable {
            let userLocks: [Ellipse]
        }
        let filter: (Result<Locks, Error>) -> () = { res in
            switch res {
            case .success(let result):
                guard let lock = result.userLocks.filter({$0.macId == macId}).first,
                    let pin = lock.pinCode else { return  completion(.failure(EllipseError.pinCodeNotFound(macId))) }
                completion(.success(pin))
            case .failure(let error):
                completion(.failure(error))
            }
            
        }
        send(.get(.locks, headers: headers), completion: filter)
    }
}

fileprivate extension Session {
    func updateFleet(completion: @escaping (Result<Void, Error>) -> ()) {
        struct Fleet: Codable {
            let fleetId: Int
            let operatorId: Int
            let fleetKey: String
        }
        let handle: (Result<Fleet, Error>) -> () = { result in
            switch result {
            case .success(let fleet):
                fleetId = fleet.fleetId
                self.storage.userId = fleet.operatorId
                EllipseManager.secret = fleet.fleetKey
                completion(.success(()))
            case .failure(let error):
                completion(.failure(error))
            }
        }
        send(.get(.fleet, headers: headers), completion: handle)
    }
}
