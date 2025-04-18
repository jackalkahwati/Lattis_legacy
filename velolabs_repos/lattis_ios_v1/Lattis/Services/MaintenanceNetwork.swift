//
//  MaintenanceNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

import Foundation
import Oval
import CoreLocation

struct Theft: Encodable {
    let bikeId: Int
    let tripId: Int?
}

protocol MaintenanceNetwork {
    func submit(report: DamageReport, completion: @escaping (Result<Void, Error>) -> ())
    func report(theft: Theft, completion: @escaping (Result<Void, Error>) -> ())
}

fileprivate extension API {
    static func maintanance(path: String) -> API {
        return .init(path: "maintenance/" + path)
    }
    static let damage = maintanance(path: "create-damage-report")
    static let theft = maintanance(path: "report-bike-theft")
}

extension Session: MaintenanceNetwork {
    func submit(report: DamageReport, completion: @escaping (Result<Void, Error>) -> ()) {
        upload(data: report.picture, for: .maintenance) { result in
            switch result {
            case .success(let url):
                self.send(.post(json: report.request(image: url), api: .damage), completion: completion)
            case .failure(let e):
                completion(.failure(e))
            }
            
        }
    }
    
    func report(theft: Theft, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.put(theft, api: .theft), completion: completion)
    }
}
