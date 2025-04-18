//
//  IoTNetwork.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 24.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import Foundation
import Oval

protocol IoTNetwork {
    func onboard(module: IoTModule, completion: @escaping (Result<IoTModule, Error>) -> ())
    func fetch(query: IoTModule.Query, completion: @escaping (Result<[IoTModule], Error>) -> ())
    func unassignBike(from module: IoTModule, completion: @escaping (Result<IoTModule, Error>) -> ())
}

fileprivate extension API {
    static func iot(query: IoTModule.Query) -> API {
        return .init(path: "controllers" + query.stringValue)
    }
    static func iot(id: Int, path: Path) -> API {
        return .init(path: "controllers/\(id)/" + path.rawValue)
    }
    static var iotHome: API { .init(path: "controllers") }
    
    enum Path: String {
        case unassign
    }
}

extension Session: IoTNetwork {
    func onboard(module: IoTModule, completion: @escaping (Result<IoTModule, Error>) -> ()) {
        send(.post(json: module, api: .iotHome), completion: { (result: Result<[IoTModule], Error>) in
            switch result {
            case .success(let modules):
                completion(.success(modules.first!))
            case .failure(let error):
                completion(.failure(error))
            }
        })
    }
    
    func fetch(query: IoTModule.Query, completion: @escaping (Result<[IoTModule], Error>) -> ()) {
        send(.get(.iot(query: query)), completion: completion)
    }
    
    func unassignBike(from module: IoTModule, completion: @escaping (Result<IoTModule, Error>) -> ()) {
        send(.post(json: FakeData(), api: .iot(id: module.controllerId!, path: .unassign)), completion: { (result: Result<[IoTModule], Error>) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let modules) where !modules.isEmpty:
                completion(.success(modules.first!))
            case .success:
                completion(.failure(IoTModule.Error.noModuleFound))
            }
        })
    }
}

extension IoTModule {
    enum Error: Swift.Error {
        case noModuleFound
    }
}

struct FakeData: Codable {
    
}
