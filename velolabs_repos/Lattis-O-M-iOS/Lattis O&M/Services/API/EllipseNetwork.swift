//
//  EllipseNetwork.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 5/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation
import Oval

protocol EllipseNetwork {
    func getLocks(for fleet: Fleet, completion: @escaping (Result<[Ellipse], Error>) -> ())
    func save(pinCode: [String], for lock: Ellipse, completion: @escaping (Result<Void, Error>) -> ())
    func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ())
    func firmvare(version: String?, completion: @escaping (Result<[String], Error>) -> ())
    func getFleetId(by macId: String, completion: @escaping (Result<Int?, Error>) -> ())
    func assign(lock macId: String, to fleet: Int, completion: @escaping (Result<Void, Error>) -> ())
    func removeLock(macId: String, completion: @escaping (Result<Void, Error>) -> ())
}

fileprivate extension API {
    static func locks(_ path: String) -> API {
        return .init(path: "locks/" + path)
    }
    static let getLocks = locks("get-locks")
    static let firmwareVersions = locks("firmware-versions-for-operator")
    static let firmware = locks("firmware-for-operator")
    static let fleet = locks("get-fleet-by-mac-id")
    static let secrets = locks("signed-message-and-public-key-for-operator")
    static let unassign = locks("unassign-lock-from-fleet")
    static let pin = locks("save-pin-code")
    static let remove = locks("unassign-lock-from-fleet")
}

extension Session: EllipseNetwork {
    func getLocks(for fleet: Fleet, completion: @escaping (Result<[Ellipse], Error>) -> ()) {
        send(.post(json: fleet, api: .getLocks), completion: completion)
    }
    
    public func firmvareVersions(completion: @escaping (Result<[String], Error>) -> ()) {
        send(.get(.firmwareVersions), completion: completion)
    }
    
    public func firmvare(version: String?, completion: @escaping (Result<[String], Error>) -> ()) {
        var params: [String: String] = [:]
        if let v = version {
            params["version"] = v
        }
        send(.post(json: params, api: .firmware), completion: completion)
    }
    
    func getFleetId(by macId: String, completion: @escaping (Result<Int?, Error>) -> ()) {
        struct Params: Encodable {
            let macId: String
        }
        let params = Params(macId: macId)
        struct Res: Decodable {
            let fleetId: Int?
        }
        send(.post(json: params, api: .fleet)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res.fleetId))
            case .failure(let error):
                if let e = error as? SessionError, case .unexpectedResponse = e.code {
                    completion(.success(nil))
                } else {
                    completion(.failure(error))
                }
            }
        }
    }
    
    func assign(lock macId: String, to fleet: Int, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let macId: String
            let fleetId: Int
        }
        struct Res: Decodable {
            let signedMessage: String
        }
        let params = Params(macId: macId, fleetId: fleet)
        send(.post(json: params, api: .secrets)) { (result: Result<Res, Error>) in
            switch result {
            case .success:
                completion(.success(()))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    public func save(pinCode: [String], for lock: Ellipse, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let lockId: Int
            let pinCode: [String]
        }
        let params = Params(lockId: lock.lockId, pinCode: pinCode)
        send(.post(json: params, api: .pin), completion: completion)
    }
    
    func removeLock(macId: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let macId: String
        }
        let params = Params(macId: macId)
        send(.post(json: params, api: .remove), completion: completion)
    }
}




