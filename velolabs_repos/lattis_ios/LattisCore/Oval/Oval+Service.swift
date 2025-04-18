//
//  Oval+Service.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 10.10.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import OvalAPI

fileprivate extension API {
    static func maintanance(_ path: Path) -> API {
        return .init(path: "maintenance/" + path.rawValue)
    }
    
    enum Path: String {
        case theft = "report-bike-theft"
        case damage = "create-damage-report"
    }
}


extension Session: ServiceNetwork {
    func report(damage: Damage, completion: @escaping (Result<Void, Error>) -> ()) {
        upload(data: damage.image, for: .maintenance) { (result) in
            switch result {
            case .success(let url):
                self.send(.post(json: damage.report(imageUrl: url), api: .maintanance(.damage)), completion: completion)
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func report(theft: Theft, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.put(theft, api: .maintanance(.theft)), completion: completion)
    }
}
