//
//  Ellipse+Oval.swift
//  Lattis SDK Demo
//
//  Created by Ravil Khusainov on 8/2/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import Oval
import LattisSDK

var isProduction: Bool {
    set {
        UserDefaults.standard.set(newValue, forKey: "isProduction")
        UserDefaults.standard.synchronize()
    }
    get {
        return UserDefaults.standard.bool(forKey: "isProduction")
    }
}

var apiToken: String {
    set {
        UserDefaults.standard.set(newValue, forKey: "api-token")
        UserDefaults.standard.synchronize()
    }
    get {
//        return "ea481fa8f91118bedc914de2e806f0f4b8186ba0177e48fecd7b65f8f31a0059"
        return UserDefaults.standard.string(forKey: "api-token") ?? ""
    }
}

fileprivate var endpoint: String {
    if isProduction {
        return "https://lattisappv2.lattisapi.io/api"
    } else {
//        return "http://192.168.2.40:3001/api"
        return "http://lattisapp-development.lattisapi.io/api"
    }
}
fileprivate var fleetId: Int? = nil
fileprivate let headers: [String: String] = ["api-token": apiToken]

fileprivate extension API {
    static var firmvareVersions: API { return API(endpoint: endpoint, path: "locks/firmware-versions-for-operator")}
    static var firmvare: API { return API(endpoint: endpoint, path: "locks/firmware-for-operator")}
    static var firmvareLog: API { return API(endpoint: endpoint, path: "locks/firmware-log-for-operator")}
    static var registration: API { return API(endpoint: endpoint, path: "locks/registration-for-operator")}
    static var signedMessage: API { return API(endpoint: endpoint, path: "locks/signed-message-and-public-key-for-operator")}
    static var fleet: API { return API(endpoint: endpoint, path: "fleet/fleet-of-token")}
    static var pinCode: API { return API(endpoint: endpoint, path: "locks/save-pin-code")}
    static var locks: API { return API(endpoint: endpoint, path: "locks/get-locks")}
}

extension Session: Network {
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
            let handle: (Result<Res, Error>) -> () = { res in
                switch res {
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
                    let pin = lock.pinCode else { return completion(.failure(EllipseError.pinCodeNotFound(macId))) }
                completion(.success(pin))
            case .failure(let error):
                completion(.failure(error))
            }
        }
        send(.get(.locks, headers: headers), completion: filter)
    }
}

fileprivate extension Oval.Session {
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

extension String {
    public var bytesArray: [UInt8]? {
        if count % 2 == 1 {
            print("cannot convert \(self) to bytes string. Odd number of digits.")
            return nil
        }
        
        var bytes:[UInt8] = [UInt8]()
        var index:Int = 0
        while index < count {
            let startIndex = self.index(self.startIndex, offsetBy: index)
            let endIndex = self.index(startIndex, offsetBy: 2)
            let subString = self[startIndex..<endIndex]
            if let value = UInt8(subString, radix: 16) {
                bytes.append(value)
            }
            
            index += 2
        }
        
        return bytes
    }
}
